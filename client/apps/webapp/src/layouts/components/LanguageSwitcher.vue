<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import type { SupportedLocale } from "@/i18n";
import { DEFAULT_LOCALE, LANGUAGES, setLocale } from "@/i18n";
import PhGlobeLight from "~icons/ph/globe-light";

withDefaults(
	defineProps<{
		displayMode?: "icon" | "select";
	}>(),
	{
		displayMode: "icon",
	},
);

const selectedLanguage = ref(DEFAULT_LOCALE);
const { t } = useI18n();

/**
 * Handles the language change logic.
 * @param languageCode The code of the language to switch to.
 */
async function changeLanguage(languageCode: SupportedLocale) {
	await setLocale(languageCode);
	selectedLanguage.value = languageCode;
}

const handleSelectChange = () => {
	changeLanguage(selectedLanguage.value ?? DEFAULT_LOCALE);
};
</script>

<template>
  <div v-if="displayMode === 'select'" class="flex items-center gap-2">
    <label for="language-select" class="text-sm font-medium">
      {{ t("navigation.language") }}
    </label>
    <select
      id="language-select"
      v-model="selectedLanguage"
      @change="handleSelectChange"
      class="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      <option
        v-for="language in LANGUAGES"
        :key="language.code"
        :value="language.code"
      >
        {{ language.name }}
      </option>
    </select>
  </div>

  <DropdownMenu v-else>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon">
        <span class="sr-only">Select Language</span>
        <PhGlobeLight />
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end">
      <DropdownMenuItem
        v-for="lang in LANGUAGES"
        :key="lang.code"
        @click="changeLanguage(lang.code)"
      >
        {{ lang.name }}
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
</template>
