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
	"./locales/en/resume.json": {
		default: {
			resume: {
				title: "Resume Generator",
				subtitle: "Create a professional resume in minutes",
				generate: "Generate PDF",
				generating: "Generating...",
				previewButton: "Preview",
				download: "Download PDF",
				clearForm: "Clear Form",
				saveProgress: "Save Progress",
				sections: {
					personalInfo: "Personal Information",
					workExperience: "Work Experience",
					education: "Education",
					skills: "Skills",
					languages: "Languages",
					projects: "Projects",
					noEntries: "No entries yet",
				},
				fields: {
					name: "Full Name",
					fullName: "Full Name",
					label: "Job Title",
					email: "Email",
					phone: "Phone",
					url: "Website",
					linkedin: "LinkedIn",
					github: "GitHub",
					summary: "Professional Summary",
					company: "Company",
					position: "Position",
					startDate: "Start Date",
					endDate: "End Date",
					current: "I currently work here",
					location: "Location",
					highlights: "Achievements",
					institution: "Institution",
					area: "Field of Study",
					studyType: "Degree Type",
					score: "GPA",
					categoryName: "Category",
					skillLevel: "Proficiency",
					keywords: "Skills",
					language: "Language",
					fluency: "Fluency",
					projectName: "Project Name",
					description: "Description",
					entity: "Organization",
					skills: "Skills",
				},
				placeholders: {
					name: "Jane Doe",
					label: "Software Engineer",
					email: "jane.doe@example.com",
					phone: "+1 (555) 123-4567",
					url: "https://janedoe.com",
					summary:
						"Experienced software engineer with a passion for building scalable web applications...",
					company: "ACME Corporation",
					position: "Senior Developer",
					location: "San Francisco, CA",
					highlight:
						"Led team of 5 developers to deliver project 2 weeks ahead of schedule",
					institution: "Stanford University",
					area: "Computer Science",
					studyType: "Bachelor of Science",
					score: "3.8",
					categoryName: "Programming Languages",
					skill: "JavaScript, TypeScript, Python",
					language: "English",
					fluency: "Native",
					projectName: "E-commerce Platform",
					description:
						"Built a modern e-commerce platform using React and Node.js",
					addSkill: "Add a skill keyword",
				},
				buttons: {
					add: "Add",
					remove: "Remove",
					addExperience: "Add Work Experience",
					addEducation: "Add Education",
					addSkillCategory: "Add Skill Category",
					addLanguage: "Add Language",
					addProject: "Add Project",
					addHighlight: "Add Achievement",
				},
				validation: {
					required: "This field is required",
					email: "Must be a valid email address",
					url: "Must be a valid URL",
					date: "Must be a valid date (YYYY-MM-DD)",
					dateRange: "End date must be after start date",
					maxLength: "Maximum length is {{max}} characters",
					minContent:
						"Resume must have at least one of: work experience, education, or skills",
					minSkills: "At least one skill is required",
				},
				errors: {
					title: "Error",
					retry: "Try Again",
					retryIn: "Try again in {seconds}s",
					dismiss: "Dismiss",
					invalidData: "Invalid resume data. Please check all fields.",
					templateRendering: "Failed to render resume template.",
					pdfGeneration: "Failed to generate PDF. Please try again.",
					pdfTimeout:
						"PDF generation took too long. Please try again with simpler content.",
					rateLimit:
						"Too many requests. Please wait {{seconds}} seconds before trying again.",
					rate_limit_exceeded:
						"Too many requests. Please wait {seconds} seconds before trying again.",
					pdf_generation_timeout:
						"PDF generation took too long. Please try again with simpler content.",
					pdf_generation_error: "Failed to generate PDF. Please try again.",
					template_rendering_error:
						"Failed to render resume template. Please check your data.",
					maliciousContent: "Content contains potentially unsafe characters.",
					malicious_content: "Content contains potentially unsafe characters.",
					validation_error: "Please correct the errors in the form.",
					invalid_resume_data:
						"Resume data is invalid. Please check required fields.",
					unknown: "An unexpected error occurred. Please try again later.",
				},
				success: {
					generated: "Resume generated successfully!",
					downloaded: "Resume downloaded successfully!",
				},
				form: {
					generate: "Generate Resume",
				},
				loading: {
					generating: "Generating your resume...",
					please_wait: "This will take just a moment",
					rendering: "Rendering template...",
					compiling: "Compiling PDF...",
					downloading: "Preparing download...",
				},
				preview: {
					name_placeholder: "Your Name",
					present: "Present",
					work_experience: "Work Experience",
					education: "Education",
					skills: "Skills",
					languages: "Languages",
					projects: "Projects",
					gpa: "GPA",
					empty_state: "Start filling out the form to see your resume preview",
					hide: "Hide Preview",
				},
			},
		},
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
	"./locales/es/resume.json": {
		default: {
			resume: {
				title: "Generador de Currículums",
				subtitle: "Crea un currículum profesional en minutos",
				generate: "Generar PDF",
				generating: "Generando...",
				previewButton: "Vista Previa",
				download: "Descargar PDF",
				clearForm: "Limpiar Formulario",
				saveProgress: "Guardar Progreso",
				sections: {
					personalInfo: "Información Personal",
					workExperience: "Experiencia Laboral",
					education: "Educación",
					skills: "Habilidades",
					languages: "Idiomas",
					projects: "Proyectos",
				},
				fields: {
					name: "Nombre Completo",
					label: "Título Profesional",
					email: "Correo Electrónico",
					phone: "Teléfono",
					url: "Sitio Web",
					summary: "Resumen Profesional",
					company: "Empresa",
					position: "Cargo",
					startDate: "Fecha de Inicio",
					endDate: "Fecha de Fin",
					current: "Actualmente trabajo aquí",
					location: "Ubicación",
					highlights: "Logros",
					institution: "Institución",
					area: "Campo de Estudio",
					studyType: "Tipo de Título",
					score: "Promedio",
					categoryName: "Categoría",
					skillLevel: "Nivel",
					keywords: "Habilidades",
					language: "Idioma",
					fluency: "Fluidez",
					projectName: "Nombre del Proyecto",
					description: "Descripción",
					entity: "Organización",
				},
				placeholders: {
					name: "María García",
					label: "Ingeniera de Software",
					email: "maria.garcia@ejemplo.com",
					phone: "+34 612 345 678",
					url: "https://mariagarcia.com",
					summary:
						"Ingeniera de software experimentada con pasión por construir aplicaciones web escalables...",
					company: "Corporación ACME",
					position: "Desarrolladora Senior",
					location: "Barcelona, España",
					highlight:
						"Lideré equipo de 5 desarrolladores para entregar proyecto 2 semanas antes de lo planeado",
					institution: "Universidad Politécnica de Madrid",
					area: "Ingeniería Informática",
					studyType: "Licenciatura",
					score: "8.5",
					categoryName: "Lenguajes de Programación",
					skill: "JavaScript, TypeScript, Python",
					language: "Español",
					fluency: "Nativo",
					projectName: "Plataforma de Comercio Electrónico",
					description:
						"Construí una plataforma moderna de comercio electrónico usando React y Node.js",
				},
				buttons: {
					add: "Agregar",
					remove: "Eliminar",
					addExperience: "Agregar Experiencia",
					addEducation: "Agregar Educación",
					addSkillCategory: "Agregar Categoría de Habilidades",
					addLanguage: "Agregar Idioma",
					addProject: "Agregar Proyecto",
					addHighlight: "Agregar Logro",
				},
				validation: {
					required: "Este campo es obligatorio",
					email: "Debe ser una dirección de correo electrónico válida",
					url: "Debe ser una URL válida",
					date: "Debe ser una fecha válida (AAAA-MM-DD)",
					dateRange: "La fecha de fin debe ser posterior a la fecha de inicio",
					maxLength: "La longitud máxima es de {{max}} caracteres",
					minContent:
						"El currículum debe tener al menos uno de: experiencia laboral, educación o habilidades",
					minSkills: "Se requiere al menos una habilidad",
				},
				errors: {
					title: "Error",
					retry: "Reintentar",
					retryIn: "Reintentar en {seconds}s",
					dismiss: "Descartar",
					invalidData:
						"Datos de currículum no válidos. Por favor, verifica todos los campos.",
					templateRendering: "Error al renderizar la plantilla de currículum.",
					pdfGeneration:
						"Error al generar el PDF. Por favor, inténtalo de nuevo.",
					pdfTimeout:
						"La generación del PDF excedió el tiempo límite. Por favor, intenta de nuevo con contenido más simple.",
					rateLimit:
						"Demasiadas solicitudes. Por favor, espera {{seconds}} segundos antes de volver a intentar.",
					rate_limit_exceeded:
						"Demasiadas solicitudes. Por favor, espera {seconds} segundos antes de volver a intentar.",
					pdf_generation_timeout:
						"La generación del PDF excedió el tiempo límite. Por favor, intenta de nuevo con contenido más simple.",
					pdf_generation_error:
						"Error al generar el PDF. Por favor, inténtalo de nuevo.",
					template_rendering_error:
						"Error al renderizar la plantilla. Por favor, verifica tus datos.",
					maliciousContent:
						"El contenido contiene caracteres potencialmente inseguros.",
					malicious_content:
						"El contenido contiene caracteres potencialmente inseguros.",
					validation_error: "Por favor, corrige los errores en el formulario.",
					invalid_resume_data:
						"Los datos del currículum no son válidos. Por favor, verifica los campos requeridos.",
					unknown:
						"Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.",
				},
				success: {
					generated: "¡Currículum generado exitosamente!",
					downloaded: "¡Currículum descargado exitosamente!",
				},
				form: {
					generate: "Generar Currículum",
				},
				loading: {
					generating: "Generando tu currículum...",
					please_wait: "Esto tomará solo un momento",
					rendering: "Renderizando plantilla...",
					compiling: "Compilando PDF...",
					downloading: "Preparando descarga...",
				},
				preview: {
					name_placeholder: "Tu Nombre",
					present: "Presente",
					work_experience: "Experiencia Laboral",
					education: "Educación",
					skills: "Habilidades",
					languages: "Idiomas",
					projects: "Proyectos",
					gpa: "Promedio",
					empty_state:
						"Comienza a llenar el formulario para ver la vista previa de tu currículum",
					hide: "Ocultar Vista Previa",
				},
			},
		},
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
	resume: {
		title: "Resume Generator",
		subtitle: "Create a professional resume in minutes",
		generate: "Generate PDF",
		generating: "Generating...",
		previewButton: "Preview",
		download: "Download PDF",
		clearForm: "Clear Form",
		saveProgress: "Save Progress",
		sections: {
			personalInfo: "Personal Information",
			workExperience: "Work Experience",
			education: "Education",
			skills: "Skills",
			languages: "Languages",
			projects: "Projects",
		},
		fields: {
			name: "Full Name",
			fullName: "Full Name",
			label: "Job Title",
			email: "Email",
			phone: "Phone",
			url: "Website",
			linkedin: "LinkedIn",
			github: "GitHub",
			summary: "Professional Summary",
			company: "Company",
			position: "Position",
			startDate: "Start Date",
			endDate: "End Date",
			current: "I currently work here",
			location: "Location",
			highlights: "Achievements",
			institution: "Institution",
			area: "Field of Study",
			studyType: "Degree Type",
			score: "GPA",
			categoryName: "Category",
			skillLevel: "Proficiency",
			keywords: "Skills",
			language: "Language",
			fluency: "Fluency",
			projectName: "Project Name",
			description: "Description",
			entity: "Organization",
		},
		placeholders: {
			name: "Jane Doe",
			label: "Software Engineer",
			email: "jane.doe@example.com",
			phone: "+1 (555) 123-4567",
			url: "https://janedoe.com",
			summary:
				"Experienced software engineer with a passion for building scalable web applications...",
			company: "ACME Corporation",
			position: "Senior Developer",
			location: "San Francisco, CA",
			highlight:
				"Led team of 5 developers to deliver project 2 weeks ahead of schedule",
			institution: "Stanford University",
			area: "Computer Science",
			studyType: "Bachelor of Science",
			score: "3.8",
			categoryName: "Programming Languages",
			skill: "JavaScript, TypeScript, Python",
			language: "English",
			fluency: "Native",
			projectName: "E-commerce Platform",
			description: "Built a modern e-commerce platform using React and Node.js",
		},
		buttons: {
			add: "Add",
			remove: "Remove",
			addExperience: "Add Work Experience",
			addEducation: "Add Education",
			addSkillCategory: "Add Skill Category",
			addLanguage: "Add Language",
			addProject: "Add Project",
			addHighlight: "Add Achievement",
		},
		validation: {
			required: "This field is required",
			email: "Must be a valid email address",
			url: "Must be a valid URL",
			date: "Must be a valid date (YYYY-MM-DD)",
			dateRange: "End date must be after start date",
			maxLength: "Maximum length is {{max}} characters",
			minContent:
				"Resume must have at least one of: work experience, education, or skills",
			minSkills: "At least one skill is required",
		},
		errors: {
			title: "Error",
			retry: "Try Again",
			retryIn: "Try again in {seconds}s",
			dismiss: "Dismiss",
			invalidData: "Invalid resume data. Please check all fields.",
			templateRendering: "Failed to render resume template.",
			pdfGeneration: "Failed to generate PDF. Please try again.",
			pdfTimeout:
				"PDF generation took too long. Please try again with simpler content.",
			rateLimit:
				"Too many requests. Please wait {{seconds}} seconds before trying again.",
			rate_limit_exceeded:
				"Too many requests. Please wait {seconds} seconds before trying again.",
			pdf_generation_timeout:
				"PDF generation took too long. Please try again with simpler content.",
			pdf_generation_error: "Failed to generate PDF. Please try again.",
			template_rendering_error:
				"Failed to render resume template. Please check your data.",
			maliciousContent: "Content contains potentially unsafe characters.",
			malicious_content: "Content contains potentially unsafe characters.",
			validation_error: "Please correct the errors in the form.",
			invalid_resume_data:
				"Resume data is invalid. Please check required fields.",
			unknown: "An unexpected error occurred. Please try again later.",
		},
		success: {
			generated: "Resume generated successfully!",
			downloaded: "Resume downloaded successfully!",
		},
		form: {
			generate: "Generate Resume",
		},
		loading: {
			generating: "Generating your resume...",
			please_wait: "This will take just a moment",
			rendering: "Rendering template...",
			compiling: "Compiling PDF...",
			downloading: "Preparing download...",
		},
		preview: {
			name_placeholder: "Your Name",
			present: "Present",
			work_experience: "Work Experience",
			education: "Education",
			skills: "Skills",
			languages: "Languages",
			projects: "Projects",
			gpa: "GPA",
			empty_state: "Start filling out the form to see your resume preview",
			hide: "Hide Preview",
		},
	},
	workspace: {
		accessibility: {
			closeSelector: "Close workspace selector",
			currentWorkspace: "Current workspace is {name}",
			errorState: "Error loading workspaces",
			loadingState: "Loading workspace information",
			openSelector: "Open workspace selector",
			selectWorkspace: "Select workspace {name}",
			workspaceList: "List of available workspaces",
		},
		error: {
			apiError: "Server error. Please try again later.",
			contactSupport: "If the problem persists, please contact support.",
			loadFailed: "Failed to load workspaces",
			networkError: "Network error. Please check your connection.",
			noDefaultWorkspace: "No default workspace configured.",
			noWorkspaces: "You don't have access to any workspaces.",
			retry: "Retry",
			switchFailed: "Failed to switch workspace",
			unauthorized: "You don't have permission to access this workspace.",
			validationError: "Invalid workspace data.",
			workspaceNotFound: "Workspace not found.",
		},
		loading: {
			autoLoad: "Loading your workspace...",
			fetchingList: "Fetching workspaces...",
			switching: "Switching workspace...",
		},
		selector: {
			currentBadge: "Current",
			defaultBadge: "Default",
			loading: "Loading workspaces...",
			noWorkspaces: "No workspaces available",
			placeholder: "Choose a workspace",
			switchWorkspace: "Switch Workspace",
			title: "Select Workspace",
		},
		success: {
			loaded: "Workspace loaded",
			switched: "Workspace switched successfully",
		},
	},
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
	resume: {
		title: "Generador de Currículums",
		subtitle: "Crea un currículum profesional en minutos",
		generate: "Generar PDF",
		generating: "Generando...",
		previewButton: "Vista Previa",
		download: "Descargar PDF",
		clearForm: "Limpiar Formulario",
		saveProgress: "Guardar Progreso",
		sections: {
			personalInfo: "Información Personal",
			workExperience: "Experiencia Laboral",
			education: "Educación",
			skills: "Habilidades",
			languages: "Idiomas",
			projects: "Proyectos",
		},
		fields: {
			name: "Nombre Completo",
			fullName: "Nombre Completo",
			label: "Título Profesional",
			email: "Correo Electrónico",
			phone: "Teléfono",
			url: "Sitio Web",
			linkedin: "LinkedIn",
			github: "GitHub",
			summary: "Resumen Profesional",
			company: "Empresa",
			position: "Cargo",
			startDate: "Fecha de Inicio",
			endDate: "Fecha de Fin",
			current: "Actualmente trabajo aquí",
			location: "Ubicación",
			highlights: "Logros",
			institution: "Institución",
			area: "Campo de Estudio",
			studyType: "Tipo de Título",
			score: "Promedio",
			categoryName: "Categoría",
			skillLevel: "Nivel",
			keywords: "Habilidades",
			language: "Idioma",
			fluency: "Fluidez",
			projectName: "Nombre del Proyecto",
			description: "Descripción",
			entity: "Organización",
		},
		placeholders: {
			name: "María García",
			label: "Ingeniera de Software",
			email: "maria.garcia@ejemplo.com",
			phone: "+34 612 345 678",
			url: "https://mariagarcia.com",
			summary:
				"Ingeniera de software experimentada con pasión por construir aplicaciones web escalables...",
			company: "Corporación ACME",
			position: "Desarrolladora Senior",
			location: "Barcelona, España",
			highlight:
				"Lideré equipo de 5 desarrolladores para entregar proyecto 2 semanas antes de lo planeado",
			institution: "Universidad Politécnica de Madrid",
			area: "Ingeniería Informática",
			studyType: "Licenciatura",
			score: "8.5",
			categoryName: "Lenguajes de Programación",
			skill: "JavaScript, TypeScript, Python",
			language: "Español",
			fluency: "Nativo",
			projectName: "Plataforma de Comercio Electrónico",
			description:
				"Construí una plataforma moderna de comercio electrónico usando React y Node.js",
		},
		buttons: {
			add: "Agregar",
			remove: "Eliminar",
			addExperience: "Agregar Experiencia",
			addEducation: "Agregar Educación",
			addSkillCategory: "Agregar Categoría de Habilidades",
			addLanguage: "Agregar Idioma",
			addProject: "Agregar Proyecto",
			addHighlight: "Agregar Logro",
		},
		validation: {
			required: "Este campo es obligatorio",
			email: "Debe ser una dirección de correo electrónico válida",
			url: "Debe ser una URL válida",
			date: "Debe ser una fecha válida (AAAA-MM-DD)",
			dateRange: "La fecha de fin debe ser posterior a la fecha de inicio",
			maxLength: "La longitud máxima es de {{max}} caracteres",
			minContent:
				"El currículum debe tener al menos uno de: experiencia laboral, educación o habilidades",
			minSkills: "Se requiere al menos una habilidad",
		},
		errors: {
			title: "Error",
			retry: "Reintentar",
			retryIn: "Reintentar en {seconds}s",
			dismiss: "Descartar",
			invalidData:
				"Datos de currículum no válidos. Por favor, verifica todos los campos.",
			templateRendering: "Error al renderizar la plantilla de currículum.",
			pdfGeneration: "Error al generar el PDF. Por favor, inténtalo de nuevo.",
			pdfTimeout:
				"La generación del PDF excedió el tiempo límite. Por favor, intenta de nuevo con contenido más simple.",
			rateLimit:
				"Demasiadas solicitudes. Por favor, espera {{seconds}} segundos antes de volver a intentar.",
			rate_limit_exceeded:
				"Demasiadas solicitudes. Por favor, espera {seconds} segundos antes de volver a intentar.",
			pdf_generation_timeout:
				"La generación del PDF excedió el tiempo límite. Por favor, intenta de nuevo con contenido más simple.",
			pdf_generation_error:
				"Error al generar el PDF. Por favor, inténtalo de nuevo.",
			template_rendering_error:
				"Error al renderizar la plantilla. Por favor, verifica tus datos.",
			maliciousContent:
				"El contenido contiene caracteres potencialmente inseguros.",
			malicious_content:
				"El contenido contiene caracteres potencialmente inseguros.",
			validation_error: "Por favor, corrige los errores en el formulario.",
			invalid_resume_data:
				"Los datos del currículum no son válidos. Por favor, verifica los campos requeridos.",
			unknown:
				"Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.",
		},
		success: {
			generated: "¡Currículum generado exitosamente!",
			downloaded: "¡Currículum descargado exitosamente!",
		},
		form: {
			generate: "Generar Currículum",
		},
		loading: {
			generating: "Generando tu currículum...",
			please_wait: "Esto tomará solo un momento",
			rendering: "Renderizando plantilla...",
			compiling: "Compilando PDF...",
			downloading: "Preparando descarga...",
		},
		preview: {
			name_placeholder: "Tu Nombre",
			present: "Presente",
			work_experience: "Experiencia Laboral",
			education: "Educación",
			skills: "Habilidades",
			languages: "Idiomas",
			projects: "Proyectos",
			gpa: "Promedio",
			empty_state:
				"Comienza a llenar el formulario para ver la vista previa de tu currículum",
			hide: "Ocultar Vista Previa",
		},
	},
	workspace: {
		accessibility: {
			closeSelector: "Cerrar selector de espacios de trabajo",
			currentWorkspace: "El espacio de trabajo actual es {name}",
			errorState: "Error al cargar espacios de trabajo",
			loadingState: "Cargando información del espacio de trabajo",
			openSelector: "Abrir selector de espacios de trabajo",
			selectWorkspace: "Seleccionar espacio de trabajo {name}",
			workspaceList: "Lista de espacios de trabajo disponibles",
		},
		error: {
			apiError: "Error del servidor. Por favor intenta más tarde.",
			contactSupport: "Si el problema persiste, por favor contacta a soporte.",
			loadFailed: "Error al cargar espacios de trabajo",
			networkError: "Error de red. Por favor verifica tu conexión.",
			noDefaultWorkspace:
				"No hay espacio de trabajo predeterminado configurado.",
			noWorkspaces: "No tienes acceso a ningún espacio de trabajo.",
			retry: "Reintentar",
			switchFailed: "Error al cambiar espacio de trabajo",
			unauthorized: "No tienes permiso para acceder a este espacio de trabajo.",
			validationError: "Datos de espacio de trabajo inválidos.",
			workspaceNotFound: "Espacio de trabajo no encontrado.",
		},
		loading: {
			autoLoad: "Cargando tu espacio de trabajo...",
			fetchingList: "Obteniendo espacios de trabajo...",
			switching: "Cambiando espacio de trabajo...",
		},
		selector: {
			currentBadge: "Actual",
			defaultBadge: "Predeterminado",
			loading: "Cargando espacios de trabajo...",
			noWorkspaces: "No hay espacios de trabajo disponibles",
			placeholder: "Elige un espacio de trabajo",
			switchWorkspace: "Cambiar Espacio de Trabajo",
			title: "Seleccionar Espacio de Trabajo",
		},
		success: {
			loaded: "Espacio de trabajo cargado",
			switched: "Espacio de trabajo cambiado exitosamente",
		},
	},
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
