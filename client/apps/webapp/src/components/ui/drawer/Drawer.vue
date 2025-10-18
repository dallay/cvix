<script lang="ts" setup>
import { useForwardPropsEmits } from "reka-ui";
import type { DrawerRootEmits, DrawerRootProps } from "vaul-vue";
import { DrawerRoot } from "vaul-vue";

const props = withDefaults(defineProps<DrawerRootProps>(), {
	shouldScaleBackground: true,
});

const emits = defineEmits<DrawerRootEmits>();

const forwarded = useForwardPropsEmits(props, emits);
// Cast forwarded to a plain object for template v-bind to satisfy TS spread rules
const forwardedProps = forwarded as unknown as Record<string, unknown>;
</script>

<template>
  <DrawerRoot
    data-slot="drawer"
    v-bind="forwardedProps"
  >
    <slot />
  </DrawerRoot>
</template>
