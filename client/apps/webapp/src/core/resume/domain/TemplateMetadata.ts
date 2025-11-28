export interface TemplateParams {
	colorPalette?: string;
	fontFamily?: string;
	spacing?: string;
	density?: string;
	customParams?: Record<string, unknown>;
}

export interface TemplateMetadata {
	id: string;
	name: string;
	version: string;
	description?: string;
	supportedLocales: string[];
	previewUrl?: string;
	params?: TemplateParams;
}

export type ParamValue = string | number | bigint | Record<string, any> | null;
