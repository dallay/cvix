import type { UIDict } from "../types";

export const error: {
	en: UIDict;
	es: UIDict;
} = {
	en: {
		"404.title": "Page Not Found",
		"404.message":
			"The page you are looking for could not be found. Please return to the top or try another language.",
		"404.top.page": "Top Page",
		"404.other.languages": "View in other languages",
		// Form validation errors
		"form.email.invalid": "Invalid email address. Please enter a valid email.",
		"form.email.required": "Email address is required.",
		"form.submission.success": "Email Submitted!",
		"form.submission.error":
			"There was an error submitting your email. Please try again.",
		"form.submission.processing": "Processing...",
		"form.submission.please.wait": "Please wait...",
	},
	es: {
		"404.title": "Página No Encontrada",
		"404.message":
			"La página que estás buscando no se pudo encontrar. Por favor, vuelve al inicio o prueba en otro idioma.",
		"404.top.page": "Página Principal",
		"404.other.languages": "Ver en otros idiomas",
		// Form validation errors
		"form.email.invalid":
			"Dirección de correo inválida. Por favor ingresa un correo válido.",
		"form.email.required": "La dirección de correo es requerida.",
		"form.submission.success": "¡Correo Enviado!",
		"form.submission.error":
			"Hubo un error al enviar tu correo. Por favor intenta de nuevo.",
		"form.submission.processing": "Procesando...",
		"form.submission.please.wait": "Por favor espera...",
	},
};
