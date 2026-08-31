(function () {
  const tbody = document.getElementById("clientesTbody");
  if (!tbody) {
    return;
  }

  const campoBusca = document.getElementById("buscaCliente");
  const botaoBuscar = document.getElementById("btnBuscar");
  const seletorTamanho = document.getElementById("tamanhoPagina");
  const mensagem = document.getElementById("clientesMensagem");
  const infoPaginacao = document.getElementById("paginacaoInfo");
  const paginaAtualLabel = document.getElementById("paginaAtualLabel");
  const botaoPrimeira = document.getElementById("btnPrimeira");
  const botaoAnterior = document.getElementById("btnAnterior");
  const botaoProxima = document.getElementById("btnProxima");
  const botaoUltima = document.getElementById("btnUltima");
  const cabecalhosOrdenaveis = document.querySelectorAll(".th-sort");

  const estado = { q: "", page: 1, pageSize: 20, sort: "nome_empresarial", dir: "asc" };

  function atualizarIconesOrdenacao() {
    cabecalhosOrdenaveis.forEach((botao) => {
      const icone = botao.querySelector(".sort-icon");
      if (botao.dataset.sort === estado.sort) {
        icone.textContent = estado.dir === "asc" ? " ▲" : " ▼";
        botao.classList.add("th-sort-active");
      } else {
        icone.textContent = "";
        botao.classList.remove("th-sort-active");
      }
    });
  }

  function linhaCliente(cliente) {
    const tr = document.createElement("tr");
    const cidadeUf = [cliente.cidade, cliente.estado].filter(Boolean).join("/");
    tr.innerHTML = `
      <td>${escapeHtml(cliente.nomeEmpresarial)}</td>
      <td>${escapeHtml(cliente.cnpj)}</td>
      <td>${escapeHtml(cliente.email || "")}</td>
      <td>${escapeHtml(cliente.telefone || "")}</td>
      <td>${escapeHtml(cidadeUf)}</td>
      <td><span class="badge ${cliente.situacao === "ATIVO" ? "success" : "inativo"}">${escapeHtml(cliente.situacao)}</span></td>`;
    return tr;
  }

  function escapeHtml(valor) {
    const div = document.createElement("div");
    div.textContent = valor == null ? "" : String(valor);
    return div.innerHTML;
  }

  function atualizarPaginacao(dados) {
    const totalItens = Number(dados.totalItems) || 0;
    const totalPaginas = Number(dados.totalPages) || 0;
    const inicio = totalItens === 0 ? 0 : (dados.page - 1) * dados.pageSize + 1;
    const fim = Math.min(dados.page * dados.pageSize, totalItens);
    infoPaginacao.textContent = totalItens === 0
      ? "Nenhum cliente encontrado"
      : `Mostrando ${inicio}–${fim} de ${totalItens} clientes`;
    paginaAtualLabel.textContent = totalPaginas === 0 ? "" : `Página ${dados.page} de ${totalPaginas}`;

    botaoPrimeira.disabled = dados.page <= 1;
    botaoAnterior.disabled = dados.page <= 1;
    botaoProxima.disabled = dados.page >= totalPaginas;
    botaoUltima.disabled = dados.page >= totalPaginas;

    estado.page = dados.page;
    estado.totalPages = totalPaginas;
  }

  async function carregar() {
    mensagem.textContent = "";
    const parametros = new URLSearchParams({
      q: estado.q,
      page: String(estado.page),
      pageSize: String(estado.pageSize),
      sort: estado.sort,
      dir: estado.dir,
    });
    try {
      const resposta = await fetch(`/api/clientes?${parametros.toString()}`);
      const dados = await resposta.json();
      if (!resposta.ok) {
        throw new Error(dados.error || "Não foi possível carregar os clientes.");
      }
      tbody.innerHTML = "";
      dados.items.forEach((cliente) => tbody.appendChild(linhaCliente(cliente)));
      atualizarPaginacao(dados);
      atualizarIconesOrdenacao();
    } catch (error) {
      mensagem.textContent = error.message;
    }
  }

  botaoBuscar.addEventListener("click", () => {
    estado.q = campoBusca.value.trim();
    estado.page = 1;
    carregar();
  });
  campoBusca.addEventListener("keydown", (evento) => {
    if (evento.key === "Enter") {
      evento.preventDefault();
      botaoBuscar.click();
    }
  });

  seletorTamanho.addEventListener("change", () => {
    estado.pageSize = Number(seletorTamanho.value);
    estado.page = 1;
    carregar();
  });

  cabecalhosOrdenaveis.forEach((botao) => {
    botao.addEventListener("click", () => {
      const coluna = botao.dataset.sort;
      if (estado.sort === coluna) {
        estado.dir = estado.dir === "asc" ? "desc" : "asc";
      } else {
        estado.sort = coluna;
        estado.dir = "asc";
      }
      estado.page = 1;
      carregar();
    });
  });

  botaoPrimeira.addEventListener("click", () => {
    estado.page = 1;
    carregar();
  });
  botaoAnterior.addEventListener("click", () => {
    estado.page = Math.max(1, estado.page - 1);
    carregar();
  });
  botaoProxima.addEventListener("click", () => {
    estado.page = Math.min(estado.totalPages || estado.page + 1, estado.page + 1);
    carregar();
  });
  botaoUltima.addEventListener("click", () => {
    estado.page = estado.totalPages || estado.page;
    carregar();
  });

  atualizarIconesOrdenacao();
  carregar();
})();
