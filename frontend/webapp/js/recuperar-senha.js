let identidadeConfirmada = null;
let mfaObrigatorio = false;

document
  .getElementById("identidadeForm")
  ?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const erro = document.getElementById("identidadeErro");
    erro.textContent = "";
    const identidade = document.getElementById("identidade").value.trim();
    const payload = identidade.includes("@")
      ? { email: identidade }
      : { login: identidade };

    try {
      const response = await fetch("/api/auth/password/forgot", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.error || "Não foi possível localizar o usuário.");
      }
      identidadeConfirmada = identidade;
      mfaObrigatorio = Boolean(data.mfaRequired);
      document.getElementById("identidadeForm").classList.add("hidden");
      document.getElementById("redefinirForm").classList.remove("hidden");
      if (mfaObrigatorio) {
        document.getElementById("mfaSection").classList.remove("hidden");
        document.getElementById("mfaCode").required = true;
      }
    } catch (error) {
      erro.textContent = error.message;
    }
  });

document
  .getElementById("redefinirForm")
  ?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const erro = document.getElementById("redefinirErro");
    const sucesso = document.getElementById("redefinirSucesso");
    erro.textContent = "";
    sucesso.classList.add("hidden");

    const novaSenha = document.getElementById("novaSenha").value;
    const confirmarSenha = document.getElementById("confirmarSenha").value;
    if (novaSenha !== confirmarSenha) {
      erro.textContent = "As senhas informadas não coincidem.";
      return;
    }

    const payload = identidadeConfirmada.includes("@")
      ? { email: identidadeConfirmada }
      : { login: identidadeConfirmada };
    payload.novaSenha = novaSenha;
    if (mfaObrigatorio) {
      payload.code = document.getElementById("mfaCode").value;
    }

    try {
      const response = await fetch("/api/auth/password/reset", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.error || "Não foi possível redefinir a senha.");
      }
      sucesso.textContent =
        "Senha redefinida com sucesso. Você já pode entrar no sistema.";
      sucesso.classList.remove("hidden");
      event.target.reset();
      event.target.querySelector("button").disabled = true;
    } catch (error) {
      erro.textContent = error.message;
    }
  });
