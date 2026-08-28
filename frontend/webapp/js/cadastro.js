document
  .getElementById("cadastroForm")
  ?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const mensagem = document.getElementById("cadastroMensagem");
    const payload = {
      nome: document.getElementById("nome").value.trim(),
      login: document.getElementById("login").value.trim(),
      email: document.getElementById("email").value.trim(),
      senha: document.getElementById("senha").value,
    };
    try {
      const response = await fetch("/api/usuarios", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = await response.json();
      if (!response.ok)
        throw new Error(data.error || "Não foi possível concluir o cadastro.");
      mensagem.className = "form-success";
      mensagem.textContent =
        "Usuário cadastrado. Você já pode entrar no sistema.";
      event.target.reset();
    } catch (error) {
      mensagem.className = "form-error";
      mensagem.textContent = error.message;
    }
  });
