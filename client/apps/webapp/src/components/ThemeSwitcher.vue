<script setup lang="ts">
import { Button } from "@/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useTheme } from "@/composables/useTheme";
import PhMonitorLight from "~icons/ph/monitor-light";
import PhMoonLight from "~icons/ph/moon-light";
import PhSunLight from "~icons/ph/sun-light";

const { theme, resolvedTheme, setTheme } = useTheme();

const themes = [
	{ value: "light", label: "Light", icon: PhSunLight },
	{ value: "dark", label: "Dark", icon: PhMoonLight },
	{ value: "system", label: "System", icon: PhMonitorLight },
] as const;
</script>

<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon">
        <span class="sr-only">Select theme</span>
        <PhSunLight class="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
        <PhMoonLight class="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end">
      <DropdownMenuItem
        v-for="themeOption in themes"
        :key="themeOption.value"
        @click="setTheme(themeOption.value)"
        :class="{ 'bg-accent': theme === themeOption.value }"
      >
        <component :is="themeOption.icon" class="mr-2 h-4 w-4" />
        {{ themeOption.label }}
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
</template>
