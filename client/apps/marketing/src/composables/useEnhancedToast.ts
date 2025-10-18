import { h } from "vue";
import { toast } from "vue-sonner";
import { type Lang, useTranslations } from "@/i18n";

export interface ToastOptions {
	lang?: Lang;
	duration?: number;
	icon?: string;
}

export function useEnhancedToast(options: ToastOptions = {}) {
	const { lang = "en", duration = 4000 } = options;
	const t = useTranslations(lang);

	const showSuccessToast = (email: string, customTitle?: string) => {
		toast({
			title: customTitle || t("form.submission.success"),
			description: h(
				"div",
				{
					class: "mt-2 flex items-center gap-2 text-sm text-muted-foreground",
				},
				[
					h("span", "✨"),
					h(
						"code",
						{
							class: "px-2 py-1 bg-muted rounded text-xs font-mono",
						},
						email,
					),
				],
			),
			duration,
		});
	};

	const showErrorToast = (error?: string, customTitle?: string) => {
		toast({
			title: customTitle || t("form.submission.error"),
			description: error ? h("p", { class: "mt-1 text-sm" }, error) : undefined,
			variant: "destructive",
			duration: duration + 1000, // Show errors a bit longer
		});
	};

	const showLoadingToast = (message?: string) => {
		return toast({
			title: message || t("form.submission.processing"),
			description: h(
				"div",
				{
					class: "flex items-center gap-2 mt-2",
				},
				[
					h("div", {
						class:
							"w-4 h-4 border-2 border-muted-foreground/30 border-t-muted-foreground rounded-full animate-spin",
					}),
					h(
						"span",
						{ class: "text-sm text-muted-foreground" },
						t("form.submission.please.wait"),
					),
				],
			),
			duration: Number.POSITIVE_INFINITY,
		});
	};

	return {
		showSuccessToast,
		showErrorToast,
		showLoadingToast,
	};
}
