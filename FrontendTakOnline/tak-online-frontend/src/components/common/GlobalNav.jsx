import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { sendContact } from "../../services/contactService";

function GlobalNav() {
  const [isOpen, setIsOpen] = useState(false);
  const [topic, setTopic] = useState("BUG");
  const [reporterEmail, setReporterEmail] = useState("");
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState("");
  const [sentMessage, setSentMessage] = useState("");

  function openModal() {
    setFormError("");
    setSentMessage("");
    setIsOpen(true);
  }

  function closeModal() {
    setIsOpen(false);
  }

  useEffect(() => {
    if (!isOpen) return;

    function handleEscape(event) {
      if (event.key === "Escape") {
        setIsOpen(false);
      }
    }

    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [isOpen]);

  async function handleSubmit(event) {
    event.preventDefault();
    setFormError("");
    setSentMessage("");

    try {
      setSubmitting(true);
      await sendContact({
        reporterEmail,
        topic,
        message,
        pageUrl: window.location.href,
      });

      setSentMessage("Gracias. El mensaje se ha enviado.");
      setMessage("");
      setTopic("BUG");
      setReporterEmail("");
    } catch (error) {
      setFormError(error?.message || "No se pudo enviar el mensaje.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <nav className="global-nav" aria-label="Main navigation">
        <Link className="global-nav-link brand" to="/">
          Tak Online
        </Link>
        <button type="button" className="global-nav-link" onClick={openModal}>
          Contact me
        </button>
      </nav>

      {isOpen && (
        <div className="contact-modal-backdrop" onClick={closeModal} role="presentation">
          <div
            className="contact-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="contact-modal-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="contact-modal-header">
              <h2 id="contact-modal-title">Contact me</h2>
              <button type="button" className="contact-close" onClick={closeModal} aria-label="Close contact form">
                &times;
              </button>
            </div>

            <form className="contact-form" onSubmit={handleSubmit}>
              <div className="contact-radio-group" role="radiogroup" aria-label="Email subject">
                <label>
                  <input
                    type="radio"
                    name="topic"
                    value="BUG"
                    checked={topic === "BUG"}
                    onChange={(event) => setTopic(event.target.value)}
                  />
                  Bug
                </label>
                <label>
                  <input
                    type="radio"
                    name="topic"
                    value="SUGGESTION"
                    checked={topic === "SUGGESTION"}
                    onChange={(event) => setTopic(event.target.value)}
                  />
                  Sugerencia
                </label>
              </div>

              <label htmlFor="contact-reporter-email">Tu correo</label>
              <input
                id="contact-reporter-email"
                type="email"
                value={reporterEmail}
                onChange={(event) => setReporterEmail(event.target.value)}
                placeholder="tuemail@dominio.com"
                required
              />

              <label htmlFor="contact-message">Mensaje</label>
              <textarea
                id="contact-message"
                value={message}
                onChange={(event) => setMessage(event.target.value)}
                placeholder="Cuéntame el bug o tu sugerencia..."
                rows={5}
                required
              />

              {formError && <p className="contact-feedback error">{formError}</p>}
              {sentMessage && <p className="contact-feedback success">{sentMessage}</p>}

              <button type="submit" className="contact-send" disabled={submitting}>
                {submitting ? "Enviando..." : "Enviar correo"}
              </button>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

export default GlobalNav;
