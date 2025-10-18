// Test fixture data for locale loading tests
export const enRegisterValidation = {
	"firstName-min": "First name must be at least 2 characters.",
	"lastName-min": "Last name must be at least 2 characters.",
	"email-invalid": "Please enter a valid email address.",
	"password-min": "Password must be at least 8 characters.",
	"password-uppercase": "Password must include at least one uppercase letter.",
	"password-lowercase": "Password must include at least one lowercase letter.",
	"password-number": "Password must include at least one number.",
	"password-special": "Password must include at least one special character.",
	"password-match": "Passwords do not match.",
};

export const esRegisterValidation = {
	"firstName-min": "El nombre debe tener al menos 2 caracteres.",
	"lastName-min": "El apellido debe tener al menos 2 caracteres.",
	"email-invalid":
		"Por favor, introduce una dirección de correo electrónico válida.",
	"password-min": "La contraseña debe tener al menos 8 caracteres.",
	"password-uppercase":
		"La contraseña debe incluir al menos una letra mayúscula.",
	"password-lowercase":
		"La contraseña debe incluir al menos una letra minúscula.",
	"password-number": "La contraseña debe incluir al menos un número.",
	"password-special":
		"La contraseña debe incluir al menos un carácter especial.",
	"password-match": "Las contraseñas no coinciden.",
};

export const enLoginForm = {
	username: "Username",
	"username-placeholder": "Enter your username",
	password: "Password",
	"password-placeholder": "Enter your password",
	rememberMe: "Remember Me",
	submit: "Login",
	forgotPassword: "Forgot Password?",
	loading: "Logging in...",
	register: "Don't have an account?",
	"register-link": "Sign up",
	validation: {
		"username-required": "Username is required",
		"password-required": "Password is required",
	},
};

export const esLoginForm = {
	username: "Usuario",
	"username-placeholder": "Ingrese su usuario",
	password: "Contraseña",
	"password-placeholder": "Ingrese su contraseña",
	rememberMe: "Recuérdame",
	submit: "Iniciar sesión",
	forgotPassword: "¿Olvidó su contraseña?",
	loading: "Iniciando sesión...",
	register: "¿No tienes una cuenta?",
	"register-link": "Regístrate",
	validation: {
		"username-required": "El usuario es obligatorio",
		"password-required": "La contraseña es obligatoria",
	},
};
