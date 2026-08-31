const PERFIL_KEY = "dominus.perfil";

const HOME_POR_PERFIL = {
  ADMINISTRADOR: "admin.html",
  GERENTE: "gerente.html",
  OPERADOR: "sistema.html",
};

const MENU_POR_PERFIL = {
  ADMINISTRADOR: [
    { href: "admin.html", label: "Usuários & MFA" },
    { href: "gerente.html", label: "Dashboard Gerencial" },
    { href: "sistema.html", label: "Clientes & Financeiro" },
  ],
  GERENTE: [
    { href: "gerente.html", label: "Dashboard Gerencial" },
    { href: "sistema.html", label: "Clientes & Financeiro" },
  ],
  OPERADOR: [{ href: "sistema.html", label: "Clientes & Financeiro" }],
};

const BADGE_POR_PERFIL = {
  ADMINISTRADOR: "admin",
  GERENTE: "gerente",
  OPERADOR: "cliente",
};

// Usado apenas quando a sessão do navegador não guarda o perfil (acesso direto
// pela URL): a página aberta indica o menu mínimo a exibir.
const PERFIL_POR_PAGINA = {
  "admin.html": "ADMINISTRADOR",
  "gerente.html": "GERENTE",
  "sistema.html": "OPERADOR",
};

function paginaAtual() {
  return window.location.pathname.split("/").pop() || "index.html";
}

function salvarPerfil(perfil) {
  try {
    sessionStorage.setItem(PERFIL_KEY, perfil);
  } catch (error) {
    /* navegador sem armazenamento: a navegação usa o perfil da página */
  }
}

function perfilAtual() {
  let armazenado = null;
  try {
    armazenado = sessionStorage.getItem(PERFIL_KEY);
  } catch (error) {
    armazenado = null;
  }
  if (armazenado && MENU_POR_PERFIL[armazenado]) {
    return armazenado;
  }
  return PERFIL_POR_PAGINA[paginaAtual()] || "OPERADOR";
}

function homeDoPerfil(perfil) {
  return HOME_POR_PERFIL[perfil] || "sistema.html";
}

function irParaHome(perfil) {
  window.location.href = homeDoPerfil(perfil || perfilAtual());
}

async function sair() {
  try {
    await fetch("/api/auth/logout", { method: "POST" });
  } catch (error) {
    /* mesmo sem resposta do servidor a sessão local é encerrada */
  }
  try {
    sessionStorage.removeItem(PERFIL_KEY);
  } catch (error) {
    /* nada a limpar */
  }
  window.location.href = "index.html";
}

function montarSidebar() {
  const sidebar = document.querySelector("[data-nav]");
  if (!sidebar) {
    return;
  }
  const perfil = perfilAtual();
  const atual = paginaAtual();
  const home = homeDoPerfil(perfil);
  const links = MENU_POR_PERFIL[perfil]
    .map(
      (item) =>
        `<a href="${item.href}"${item.href === atual ? ' class="active"' : ""}>${item.label}</a>`,
    )
    .join("");

  sidebar.classList.add("sidebar");
  sidebar.innerHTML = `
    <a class="sidebar-brand" href="${home}">Dominus Gestor</a>
    <span class="badge ${BADGE_POR_PERFIL[perfil]}">${perfil}</span>
    <nav>
      <a href="${home}" class="nav-home">Início</a>
      ${links}
    </nav>
    <div class="sidebar-footer">
      <button type="button" class="btn-link" data-sair>Sair</button>
    </div>`;
  sidebar.querySelector("[data-sair]").addEventListener("click", sair);
}

function montarCabecalho() {
  const header = document.querySelector(".content > header");
  if (!header) {
    return;
  }
  const home = homeDoPerfil(perfilAtual());
  header.classList.add("page-header");
  const acoes = document.createElement("div");
  acoes.className = "header-actions";
  acoes.innerHTML = `<a class="btn-sm" href="${home}">Voltar ao início</a>`;
  header.appendChild(acoes);
}

document.addEventListener("DOMContentLoaded", () => {
  montarSidebar();
  montarCabecalho();
});

window.DominusNav = {
  salvarPerfil,
  perfilAtual,
  homeDoPerfil,
  irParaHome,
  sair,
};
