<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldDescription,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSet,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import type { Certificate } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const certificates = defineModel<Certificate[]>({
	default: () => [],
});

const addCertificate = () => {
	certificates.value.push({
		name: "",
		date: "",
		issuer: "",
		url: "",
	});
};

const removeCertificate = (index: number) => {
	certificates.value.splice(index, 1);
};

const hasCertificates = computed(() => certificates.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldLegend>{{ t('resume.sections.certificates') }}</FieldLegend>
		<FieldDescription>
			{{ t('resume.actions.descriptions.certificates') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addCertificate"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addCertificate') }}
				</Button>
			</div>

			<div
				v-if="hasCertificates"
				class="space-y-6"
			>
				<div
					v-for="(certificate, certIndex) in certificates"
					:key="certIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.certificate', { number: certIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeCertificate(certIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`certificate-name-${certIndex}`">
									{{ t('resume.fields.certificateName') }}
								</FieldLabel>
								<Input
									:id="`certificate-name-${certIndex}`"
									v-model="certificate.name"
									type="text"
									:placeholder="t('resume.placeholders.certificateName')"
									:data-testid="`certificate-name-${certIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`certificate-date-${certIndex}`">
									{{ t('resume.fields.date') }}
								</FieldLabel>
								<Input
									:id="`certificate-date-${certIndex}`"
									v-model="certificate.date"
									type="date"
									:data-testid="`certificate-date-${certIndex}`"
									required
								/>
							</Field>
						</div>

						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`certificate-issuer-${certIndex}`">
									{{ t('resume.fields.issuer') }}
								</FieldLabel>
								<Input
									:id="`certificate-issuer-${certIndex}`"
									v-model="certificate.issuer"
									type="text"
									:placeholder="t('resume.placeholders.issuer')"
									:data-testid="`certificate-issuer-${certIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`certificate-url-${certIndex}`">
									{{ t('resume.fields.url') }}
								</FieldLabel>
								<Input
									:id="`certificate-url-${certIndex}`"
									v-model="certificate.url"
									type="url"
									:placeholder="t('resume.placeholders.certificateUrl')"
									:data-testid="`certificate-url-${certIndex}`"
								/>
							</Field>
						</div>
					</FieldGroup>
				</div>
			</div>

			<div
				v-else
				class="text-center py-8 border border-dashed border-border rounded-lg bg-muted/50"
			>
				<p class="text-sm text-muted-foreground">
					{{ t('resume.actions.empty.certificates') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addCertificate"
				>
					{{ t('resume.actions.addFirstCertificate') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

