<script setup lang="ts">
import { Button } from "../button";
import { Input } from "../input";
import { cn } from "../../../lib/utils";

interface Props {
  emailPlaceholder?: string;
  buttonText?: string;
  lang?: string;
  size?: "default" | "sm" | "lg";
  alignment?: "left" | "center" | "right";
  class?: string;
}

const props = withDefaults(defineProps<Props>(), {
  emailPlaceholder: "Enter your email",
  buttonText: "Join",
  size: "default",
  alignment: "center",
});
</script>

<template>
  <div :class="cn('flex flex-col gap-4 w-full max-w-sm', props.class)">
    <div class="flex gap-2">
      <Input :placeholder="emailPlaceholder" />
      <Button>{{ buttonText }}</Button>
    </div>
  </div>
</template>
