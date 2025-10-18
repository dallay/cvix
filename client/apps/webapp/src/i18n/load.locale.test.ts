import { describe, expect, it, vi } from "vitest";

vi.mock("@loomify/utilities", () => ({
	deepmerge: {
		all: vi.fn((objs) => {
			const deepMerge = (
				target: Record<string, unknown>,
				source: Record<string, unknown>,
			): Record<string, unknown> => {
				const result: Record<string, unknown> = { ...target };
				for (const key in source) {
					if (source[key] instanceof Object && !Array.isArray(source[key])) {
						result[key] = deepMerge(
							(result[key] as Record<string, unknown>) || {},
							source[key] as Record<string, unknown>,
						);
					} else {
						result[key] = source[key];
					}
				}
				return result;
			};
			return objs.reduce(
				(acc: Record<string, unknown>, obj: Record<string, unknown>) =>
					deepMerge(acc, obj),
				{},
			);
		}),
	},
}));

import { getLocaleModulesSync } from "./load.locales";

const enRegisterValidation = {
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

const esRegisterValidation = {
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

const enLoginForm = {
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

const esLoginForm = {
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

const mockMessages = {
	"./locales/en/global.json": {
		default: {
			global: {
				ribbon: { dev: "Development" },
				navigation: {
					dashboard: "Dashboard",
					tags: "Tags",
					audience: "Audience",
					subscribers: "Subscribers",
					account: "Account",
					settings: "Settings",
					changePassword: "Change Password",
					admin: "Admin",
					userManagement: "User Management",
					systemSettings: "System Settings",
					userManagementTooltip: "Manage system users",
					systemSettingsTooltip: "Configure system settings",
					home: "Home",
					profile: "Profile",
				},
				common: {
					auth: {
						login: "Login",
						logout: "Logout",
					},
					loading: "Loading...",
					error: "An error occurred",
					notFound: "Page not found",
					backToHome: "Back to Home",
					search: "Search",
					submit: "Submit",
					cancel: "Cancel",
					save: "Save",
					delete: "Delete",
					edit: "Edit",
					view: "View",
					update: "Update",
					create: "Create",
					confirm: "Confirm",
					yes: "Yes",
					no: "No",
				},
			},
		},
	},
	"./locales/en/error.json": {
		default: {
			error: {
				title: "Error Occurred",
				message: "An unexpected error has occurred. Please try again later.",
				backToHome: "Back to Home",
			},
		},
	},
	"./locales/en/login.json": {
		default: {
			login: {
				title: "Login",
				description: "Enter your credentials below to login to your account.",
				form: enLoginForm,
			},
		},
	},
	"./locales/en/register.json": {
		default: { register: { form: { validation: enRegisterValidation } } },
	},
	"./locales/es/global.json": {
		default: {
			global: {
				ribbon: { dev: "Desarrollo" },
				navigation: {
					dashboard: "Tablero",
					tags: "Etiquetas",
					audience: "Audiencia",
					subscribers: "Suscriptores",
					account: "Cuenta",
					settings: "Configuración",
					changePassword: "Cambiar contraseña",
					admin: "Administración",
					userManagement: "Gestión de usuarios",
					systemSettings: "Configuración del sistema",
					userManagementTooltip: "Administrar usuarios del sistema",
					systemSettingsTooltip: "Configurar los ajustes del sistema",
					home: "Inicio",
					profile: "Perfil",
				},
				common: {
					auth: {
						login: "Iniciar sesión",
						logout: "Cerrar sesión",
					},
					loading: "Cargando...",
					error: "Ocurrió un error",
					notFound: "Página no encontrada",
					backToHome: "Volver al inicio",
					search: "Buscar",
					submit: "Enviar",
					cancel: "Cancelar",
					save: "Guardar",
					delete: "Eliminar",
					edit: "Editar",
					view: "Ver",
					update: "Actualizar",
					create: "Crear",
					confirm: "Confirmar",
					yes: "Sí",
					no: "No",
				},
			},
		},
	},
	"./locales/es/error.json": {
		default: {
			error: {
				title: "Ocurrió un error",
				message:
					"Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde.",
				backToHome: "Volver al inicio",
			},
		},
	},
	"./locales/es/login.json": {
		default: {
			login: {
				title: "Iniciar sesión",
				description: "Ingrese sus credenciales abajo para acceder a su cuenta.",
				form: esLoginForm,
			},
		},
	},
	"./locales/es/register.json": {
		default: { register: { form: { validation: esRegisterValidation } } },
	},
};

vi.stubGlobal("import.meta", {
	glob: vi.fn((pattern: string, options: { eager: boolean }) => {
		if (pattern === "./locales/**/*.json" && options.eager) {
			return mockMessages;
		}
		return {};
	}),
});

const expectedEnMessages = {
	error: {
		title: "Error Occurred",
		message: "An unexpected error has occurred. Please try again later.",
		backToHome: "Back to Home",
	},
	global: {
		ribbon: { dev: "Development" },
		navigation: {
			dashboard: "Dashboard",
			tags: "Tags",
			audience: "Audience",
			subscribers: "Subscribers",
			account: "Account",
			settings: "Settings",
			changePassword: "Change Password",
			admin: "Admin",
			userManagement: "User Management",
			systemSettings: "System Settings",
			userManagementTooltip: "Manage system users",
			systemSettingsTooltip: "Configure system settings",
			home: "Home",
			profile: "Profile",
		},
		common: {
			auth: {
				login: "Login",
				logout: "Logout",
			},
			loading: "Loading...",
			error: "An error occurred",
			notFound: "Page not found",
			backToHome: "Back to Home",
			search: "Search",
			submit: "Submit",
			cancel: "Cancel",
			save: "Save",
			delete: "Delete",
			edit: "Edit",
			view: "View",
			update: "Update",
			create: "Create",
			confirm: "Confirm",
			yes: "Yes",
			no: "No",
		},
	},
	login: {
		title: "Login",
		description: "Enter your credentials below to login to your account.",
		form: enLoginForm,
	},
	register: { form: { validation: enRegisterValidation } },
};

const expectedEsMessages = {
	error: {
		title: "Ocurrió un error",
		message:
			"Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde.",
		backToHome: "Volver al inicio",
	},
	global: {
		ribbon: { dev: "Desarrollo" },
		navigation: {
			dashboard: "Tablero",
			tags: "Etiquetas",
			audience: "Audiencia",
			subscribers: "Suscriptores",
			account: "Cuenta",
			settings: "Configuración",
			changePassword: "Cambiar contraseña",
			admin: "Administración",
			userManagement: "Gestión de usuarios",
			systemSettings: "Configuración del sistema",
			userManagementTooltip: "Administrar usuarios del sistema",
			systemSettingsTooltip: "Configurar los ajustes del sistema",
			home: "Inicio",
			profile: "Perfil",
		},
		common: {
			auth: {
				login: "Iniciar sesión",
				logout: "Cerrar sesión",
			},
			loading: "Cargando...",
			error: "Ocurrió un error",
			notFound: "Página no encontrada",
			backToHome: "Volver al inicio",
			search: "Buscar",
			submit: "Enviar",
			cancel: "Cancelar",
			save: "Guardar",
			delete: "Eliminar",
			edit: "Editar",
			view: "Ver",
			update: "Actualizar",
			create: "Crear",
			confirm: "Confirmar",
			yes: "Sí",
			no: "No",
		},
	},
	login: {
		title: "Iniciar sesión",
		description: "Ingrese sus credenciales abajo para acceder a su cuenta.",
		form: esLoginForm,
	},
	register: { form: { validation: esRegisterValidation } },
};

describe("getLocaleModulesSync", () => {
	it("returns merged messages for en locale", () => {
		const result = getLocaleModulesSync("en");
		expect(result).toEqual(expectedEnMessages);
	});

	it("returns merged messages for es locale", () => {
		const result = getLocaleModulesSync("es");
		expect(result).toEqual(expectedEsMessages);
	});

	it("returns empty object for unsupported locale", () => {
		const result = getLocaleModulesSync("fr");
		expect(result).toEqual({});
	});

	it("caches merged locale messages to avoid redundant merging", () => {
		const firstCall = getLocaleModulesSync("en");
		const secondCall = getLocaleModulesSync("en");
		expect(secondCall).toBe(firstCall);
	});
});
