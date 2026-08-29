let mfaPending = false;
let pendingRole = null;

const HOME_BY_ROLE = {
  ADMINISTRADOR: "admin.html",
  GERENTE: "gerente.html",
  OPERADOR: "sistema.html",
};

function irParaTelaInicial(role) {
  window.location.href = HOME_BY_ROLE[role] || "sistema.html";
}

document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const email = document.getElementById("email").value;
  const senha = document.getElementById("senha").value;

  try {
    const endpoint = mfaPending ? "/api/auth/mfa/verify" : "/api/auth/login";
    const payload = mfaPending
      ? { email, code: document.getElementById("mfaCode").value }
      : { email, senha };
    const response = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || "Não foi possível autenticar.");
    }
    if (data.mfaRequired && !mfaPending) {
      mfaPending = true;
      pendingRole = data.role;
      document.getElementById("mfaSection").classList.remove("hidden");
      document.getElementById("mfaCode").required = true;
      document.getElementById("btnLogin").textContent = "Validar código";
      return;
    }
    if (mfaPending && data.status !== "VALIDATED") {
      throw new Error("Não foi possível validar o código MFA.");
    }
    irParaTelaInicial(mfaPending ? pendingRole : data.role);
  } catch (error) {
    document.getElementById("loginError").textContent = error.message;
  }
});
