<script setup lang="ts">
import { Button } from "@/components/ui/button";
import {
	NavigationMenu,
	NavigationMenuContent,
	NavigationMenuItem,
	NavigationMenuLink,
	NavigationMenuList,
	NavigationMenuTrigger,
} from "@/components/ui/navigation-menu";

import { type Lang, useTranslatedPath, useTranslations } from "@/i18n";
import type { MenuItem } from "@/models/menu/menu.type";

const props = defineProps<{
	menuItems: MenuItem[];
	lang: Lang;
	currentPath: string;
}>();

const t = useTranslations(props.lang);
const translatePath = useTranslatedPath(props.lang);

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

// Check whether any non-external child link is active
const isDropdownActive = (children?: MenuItem[] | undefined) => {
	return !!children?.some(
		(child) =>
			child.type === "link" &&
			!isExternal(child.href) &&
			props.currentPath === translatePath(child.href),
	);
};
</script>

<template>
  <NavigationMenu>
    <NavigationMenuList>
      <template v-for="item in props.menuItems" :key="item.translationKey">
        <template v-if="item.condition">
          <NavigationMenuItem v-if="item.type === 'link'">
            <NavigationMenuLink as-child>
              <Button variant="ghost" :class="`text-base ${getLinkClass(item.href)}`">
                <a
                  :href="resolveHref(item.href)"
                  :target="isExternal(item.href) ? '_blank' : undefined"
                  :rel="isExternal(item.href) ? 'noopener noreferrer' : undefined"
                >
                  {{ t(item.translationKey) }}
                </a>
              </Button>
            </NavigationMenuLink>
          </NavigationMenuItem>

          <NavigationMenuItem v-if="item.type === 'dropdown'">
            <NavigationMenuTrigger
              class="text-base font-medium transition-colors bg-transparent hover:text-primary-700 dark:hover:text-primary-400 focus:outline-none dark:bg-transparent"
              :class="
                isDropdownActive(item.children)
                  ? 'text-primary-700 dark:text-primary-400 font-semibold'
                  : ''
              "
            >
              {{ t(item.translationKey) }}
            </NavigationMenuTrigger>
            <NavigationMenuContent>
              <ul class="grid gap-3 p-4 lg:grid-cols-[.75fr_1fr]">
                <template v-for="child in item.children" :key="child.translationKey">
                  <template v-if="child.condition && child.type === 'link'">
                    <li>
                      <NavigationMenuLink as-child>
                        <a
                          :href="resolveHref(child.href)"
                          :target="isExternal(child.href) ? '_blank' : undefined"
                          :rel="
                            isExternal(child.href) ? 'noopener noreferrer' : undefined
                          "
                          :class="`block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground ${getLinkClass(
                            child.href
                          )}`"
                        >
                          <div class="text-sm font-medium leading-none">
                            {{ t(child.translationKey) }}
                          </div>
                        </a>
                      </NavigationMenuLink>
                    </li>
                  </template>
                </template>
              </ul>
            </NavigationMenuContent>
          </NavigationMenuItem>
        </template>
      </template>
    </NavigationMenuList>
  </NavigationMenu>
</template>
