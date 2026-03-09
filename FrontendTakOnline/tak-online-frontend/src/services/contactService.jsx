const API_BASE_URL = "http://localhost:8080";

export async function sendContact(payload) {
  const response = await fetch(`${API_BASE_URL}/api/contact`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    let message = errorText || "No se pudo enviar el correo";
    try {
      const parsed = JSON.parse(errorText);
      if (parsed?.message) {
        message = parsed.message;
      }
    } catch {
      // Response is not JSON, keep plain text
    }
    throw new Error(message);
  }

  return await response.json();
}
