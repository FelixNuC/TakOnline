import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { sendContact } from "../../services/contactService";

function GlobalNav() {
  const [isOpen, setIsOpen] = useState(false);
  const [isRulesOpen, setIsRulesOpen] = useState(false);
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

  function openRulesModal() {
    setIsRulesOpen(true);
  }

  function closeRulesModal() {
    setIsRulesOpen(false);
  }

  useEffect(() => {
    if (!isOpen && !isRulesOpen) return;

    function handleEscape(event) {
      if (event.key === "Escape") {
        setIsOpen(false);
        setIsRulesOpen(false);
      }
    }

    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [isOpen, isRulesOpen]);

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
        <button type="button" className="global-nav-link" onClick={openRulesModal}>
          How to play
        </button>
        <button type="button" className="global-nav-link" onClick={openModal}>
          Contact me
        </button>
      </nav>

      {isRulesOpen && (
        <div className="contact-modal-backdrop" onClick={closeRulesModal} role="presentation">
          <div
            className="contact-modal rules-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="rules-modal-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="contact-modal-header">
              <h2 id="rules-modal-title">How to play Tak</h2>
              <button
                type="button"
                className="contact-close rules-close"
                onClick={(event) => {
                  event.preventDefault();
                  event.stopPropagation();
                  closeRulesModal();
                }}
                aria-label="Close rules"
              >
                &times;
              </button>
            </div>

            <div className="rules-body">
              <p>
                Objetivo principal: crear un camino continuo de tus piezas conectando dos lados opuestos del tablero
                antes que tu rival.
              </p>
              <ul className="rules-list">
                <li>Cada turno eliges entre Colocar o Mover.</li>
                <li>Colocar: en casilla vacia puedes poner Flat, Stand o Capstone.</li>
                <li>Solo controlas un stack si la pieza superior es tuya.</li>
                <li>Para ganar por camino, normalmente cuentan flats y capstone; un stand actua como bloqueo.</li>
              </ul>

              <div className="rules-subsection">
                <h3>Movimiento de stacks</h3>
                <ul className="rules-list">
                  <li>Seleccionas un stack propio y defines cuantas piezas tomas (Pickup).</li>
                  <li>El movimiento es en linea recta: sin diagonales.</li>
                  <li>Vas soltando piezas por el camino hasta llegar al destino final.</li>
                  <li>El pickup debe ser suficiente para cubrir la distancia del movimiento.</li>
                </ul>
              </div>

              <div className="rules-subsection">
                <h3>Tipos de pieza</h3>
                <ul className="rules-list">
                  <li>Flat: pieza base para construir caminos.</li>
                  <li>Stand (muro): bloquea caminos rivales.</li>
                  <li>Capstone: pieza fuerte que domina stacks y ayuda a abrir ruta.</li>
                </ul>
              </div>

              <div className="rules-subsection">
                <h3>Interaccion con piezas enemigas</h3>
                <ul className="rules-list">
                  <li>Puedes quedar encima de piezas enemigas en jugadas validas y pasar a controlar ese stack.</li>
                  <li>No puedes apilarte sobre una capstone enemiga.</li>
                  <li>Si una jugada rompe estas reglas, el servidor la rechaza como movimiento invalido.</li>
                </ul>
              </div>

              <div className="rules-demo-grid">
                <div className="rules-demo-card">
                  <h3>Ejemplo de stack</h3>
                  <div className="stack-static-demo" aria-hidden="true">
                    <div className="stack-demo-head">
                      <span>Base</span>
                      <span>Top</span>
                    </div>
                    <div className="stack-demo-row">
                      <span className="stack-piece white-flat" />
                      <span className="stack-piece black-flat diamond-flat" />
                      <span className="stack-piece white-flat" />
                      <span className="stack-piece black-stand" />
                      <span className="stack-piece white-capstone" />
                    </div>
                  </div>
                  <p className="rules-demo-help">
                    Stack mixto: flats, un stand y capstone arriba.
                  </p>
                </div>

                <div className="rules-demo-card">
                  <h3>Tipos de piezas</h3>
                  <div className="piece-types-demo" aria-hidden="true">
                    <div className="piece-type-item">
                      <span className="type-shape white-flat" />
                      <span>Flat</span>
                    </div>
                    <div className="piece-type-item">
                      <span className="type-shape black-flat diamond-flat" />
                      <span>Flat negra</span>
                    </div>
                    <div className="piece-type-item">
                      <span className="type-shape white-stand" />
                      <span>Stand</span>
                    </div>
                    <div className="piece-type-item">
                      <span className="type-shape white-capstone" />
                      <span>Capstone</span>
                    </div>
                  </div>
                  <p className="rules-demo-help">
                    Referencia visual rapida de piezas comunes.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

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
                                <label>
                  <input
                    type="radio"
                    name="topic"
                    value="OTHER"
                    checked={topic === "OTHER"}
                    onChange={(event) => setTopic(event.target.value)}
                  />
                  Other
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
