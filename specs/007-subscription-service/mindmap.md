<!-- Mermaid mindmap for the Subscription Service (español) -->
# Esquema: Servicio de Suscripción — Mapa Mental

```mermaid
mindmap
  root((Servicio de Suscripción))
    Objetivos
      Capturar email
      Doble opt-in
      Gestionar bajas
      Cumplimiento GDPR
    API
      POST /v1/subscriptions : Capturar email (crear pending)
      POST /v1/subscriptions/confirm : Confirmar suscripción
      POST /v1/subscriptions/unsubscribe : Dar de baja
      GET /v1/subscriptions/:id : Consultar estado
    Modelo de Datos
      Suscripción
        id: uuid
        email: string
        status: pending|confirmed|unsubscribed
        source: string (form, api, import)
        metadata: json
        created_at: timestamp
        confirmed_at: timestamp?
    Flujo
      Captura -> Validación -> Crear(pending) -> Enviar confirmación -> Confirmar -> confirmed
      Unsubscribe -> marcar unsubscribed -> emitir evento
    Validación & Seguridad
      Sanitizar input
      Rate limiting
      Protección contra bots (captcha/reputación)
      Doble opt-in para verificación
    Almacenamiento
      Tabla: subscriptions
      Indices: email, status, created_at
      TTL/retención para pending antiguos
    Integraciones
      SMTP/SES/SendGrid para emails
      Webhook a servicios downstream
      Analytics / Tracking (eventos)
    Eventos
      email_captured
      email_confirmation_sent
      email_confirmed
      email_unsubscribed
    Observabilidad
      Métricas: captures, confirmations, unsubscribes, error-rate
      Logs: request_id, source, email-hash
    Consideraciones Legales
      Consentimiento claro
      Auditoría de consentimiento
```

Notas:
- Este archivo es un mapa mental de alto nivel; ajústalo según la `spec.md` si necesitas más detalle.
