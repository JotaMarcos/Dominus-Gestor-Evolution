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
