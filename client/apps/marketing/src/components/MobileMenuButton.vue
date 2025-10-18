<script setup lang="ts">
import { SheetDescription } from "@components/ui/sheet";
import { Menu } from "lucide-vue-next";
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import {
	Sheet,
	SheetContent,
	SheetHeader,
	SheetTitle,
	SheetTrigger,
} from "@/components/ui/sheet";
import { type Lang, useTranslatedPath, useTranslations } from "@/i18n";
import type { MenuItem } from "@/models/menu/menu.type";

const props = defineProps<{
	menuItems: MenuItem[];
	lang: Lang;
	currentPath: string;
}>();

const t = useTranslations(props.lang);
const translatePath = useTranslatedPath(props.lang);

const isMobileMenuOpen = ref(false);

// Utility: detect external links (http(s), protocol-relative, mailto, tel)
const isExternal = (href: string) => {
	if (!href) return false;
	return (
		/^(https?:)?\/\//.test(href) ||
		href.startsWith("mailto:") ||
		href.startsWith("tel:")
	);
};

// Resolve href to use translated/internal path when applicable
const resolveHref = (href: string) => {
	return isExternal(href) ? href : translatePath(href);
};

// Return CSS class for active/internal links only. External links are never "active".
const getLinkClass = (href: string) => {
	if (!href || isExternal(href)) return "";
	return props.currentPath === translatePath(href)
		? "text-primary-700 dark:text-primary-400 font-semibold"
		: "";
};
</script>

<template>
  <Sheet v-model:open="isMobileMenuOpen">
    <SheetTrigger as-child>
      <Button variant="ghost" size="icon">
        <Menu class="h-6 w-6" />
        <span class="sr-only">Open menu</span>
      </Button>
    </SheetTrigger>
    <SheetContent side="left" class="w-[300px] sm:w-[400px]">
      <SheetHeader class="mb-4">
        <SheetTitle>
          <a href="/" @click="isMobileMenuOpen = false">Menu</a>
        </SheetTitle>
        <SheetDescription class="sr-only">Primary navigation menu</SheetDescription>
      </SheetHeader>
      <div class="flex flex-col gap-2">
        <template v-for="item in props.menuItems" :key="item.translationKey">
          <template v-if="item.condition">
            <Button
              v-if="item.type === 'link'"
              variant="ghost"
              as-child
              class="justify-start text-base"
              :class="getLinkClass(item.href)"
              @click="isMobileMenuOpen = false"
            >
              <a
                :href="resolveHref(item.href)"
                :target="isExternal(item.href) ? '_blank' : undefined"
                :rel="isExternal(item.href) ? 'noopener noreferrer' : undefined"
              >
                {{ t(item.translationKey) }}
              </a>
            </Button>
            <div v-if="item.type === 'dropdown'">
              <p
                class="px-4 py-2 font-semibold text-base text-gray-800 dark:text-gray-200"
              >
                {{ t(item.translationKey) }}
              </p>
              <template v-for="child in item.children" :key="child.translationKey">
                <Button
                  v-if="child.type === 'link'"
                  variant="ghost"
                  as-child
                  class="justify-start text-base ml-4"
                  :class="getLinkClass(child.href)"
                  @click="isMobileMenuOpen = false"
                >
                  <a
                    :href="resolveHref(child.href)"
                    :target="isExternal(child.href) ? '_blank' : undefined"
                    :rel="isExternal(child.href) ? 'noopener noreferrer' : undefined"
                  >
                    {{ t(child.translationKey) }}
                  </a>
                </Button>
              </template>
            </div>
          </template>
        </template>
      </div>
    </SheetContent>
  </Sheet>
</template>
