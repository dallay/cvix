// More specific type for better type safety while maintaining vue-i18n compatibility
export interface LocaleMessageObject {
	[key: string]: LocaleMessageValue;
}

export type LocaleMessageArray = LocaleMessageValue[];

export type LocaleMessageValue =
	| string
	| number
	| boolean
	| LocaleMessageObject
	| LocaleMessageArray;

export type LocaleMessage = LocaleMessageObject;
