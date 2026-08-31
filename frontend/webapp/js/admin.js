function escapeHtml(valor) {
  const div = document.createElement("div");
  div.textContent = valor == null ? "" : String(valor);
  return div.innerHTML;
}

function linhaUsuario(usuario) {
  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td>${usuario.id}</td>
    <td>${escapeHtml(usuario.nome)}</td>
    <td>${escapeHtml(usuario.email)}</td>
    <td>${escapeHtml(usuario.perfil)}</td>
    <td><span class="badge ${usuario.mfaHabilitado ? "success" : "inativo"}">${usuario.mfaHabilitado ? "Habilitado" : "Desabilitado"}</span></td>
    <td><button onclick="toggleMFA(${usuario.id})" class="btn-sm">Alterar MFA</button></td>`;
  return tr;
}

async function carregarUsuarios() {
  const tbody = document.getElementById("usuariosTbody");
  const mensagem = document.getElementById("usuariosMensagem");
  if (!tbody) {
    return;
  }
  try {
    const response = await fetch("/api/usuarios");
    const dados = await response.json();
    if (!response.ok) {
      throw new Error(dados.error || "Não foi possível carregar os usuários.");
    }
    tbody.innerHTML = "";
    dados.forEach((usuario) => tbody.appendChild(linhaUsuario(usuario)));
  } catch (error) {
    mensagem.textContent = error.message;
  }
}

async function toggleMFA(userId) {
  try {
    const response = await fetch("/api/auth/mfa/toggle", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId }),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || "Não foi possível alterar o MFA.");
    }
    window.location.reload();
  } catch (error) {
    window.alert(error.message);
  }
}

document.addEventListener("DOMContentLoaded", carregarUsuarios);
