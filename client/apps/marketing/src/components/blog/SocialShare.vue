<template>
  <div class="flex flex-col space-y-4">
    <h3 class="text-sm font-medium text-foreground">{{ t('blog.share.title') }}</h3>
    <div class="flex flex-wrap gap-2">
      <button
        @click="shareOnTwitter"
        class="inline-flex items-center px-3 py-2 text-sm bg-blue-500 hover:bg-blue-600 text-white rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        :aria-label="t('blog.share.twitter')"
      >
        <Icon name="tabler:brand-x" class="w-4 h-4 mr-2" />
        X
      </button>
      
      <button
        @click="shareOnLinkedIn"
        class="inline-flex items-center px-3 py-2 text-sm bg-blue-700 hover:bg-blue-800 text-white rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-blue-700 focus:ring-offset-2"
        :aria-label="t('blog.share.linkedin')"
      >
        <Icon name="tabler:brand-linkedin" class="w-4 h-4 mr-2" />
        LinkedIn
      </button>
      
      <button
        @click="shareOnFacebook"
        class="inline-flex items-center px-3 py-2 text-sm bg-blue-600 hover:bg-blue-700 text-white rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2"
        :aria-label="t('blog.share.facebook')"
      >
        <Icon name="tabler:brand-facebook" class="w-4 h-4 mr-2" />
        Facebook
      </button>
      
      <button
        @click="copyLink"
        class="inline-flex items-center px-3 py-2 text-sm bg-muted hover:bg-muted/80 text-muted-foreground hover:text-foreground rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
        :aria-label="t('blog.share.copy')"
      >
        <Icon :name="copied ? 'tabler:check' : 'tabler:copy'" class="w-4 h-4 mr-2" />
        {{ copied ? t('blog.share.copied') : t('blog.share.copy') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from "astro-icon/components";
import { ref } from "vue";
import { type Lang, useTranslations } from "@/i18n";

interface Props {
	title: string;
	url: string;
	lang: Lang;
}

const props = defineProps<Props>();
const t = useTranslations(props.lang);

const copied = ref(false);

const shareOnTwitter = () => {
	const text = encodeURIComponent(props.title);
	const url = encodeURIComponent(props.url);
	window.open(
		`https://twitter.com/intent/tweet?text=${text}&url=${url}`,
		"_blank",
		"width=550,height=420",
	);
};

const shareOnLinkedIn = () => {
	const url = encodeURIComponent(props.url);
	const title = encodeURIComponent(props.title);
	window.open(
		`https://www.linkedin.com/sharing/share-offsite/?url=${url}`,
		"_blank",
		"width=550,height=420",
	);
};

const shareOnFacebook = () => {
	const url = encodeURIComponent(props.url);
	window.open(
		`https://www.facebook.com/sharer/sharer.php?u=${url}`,
		"_blank",
		"width=550,height=420",
	);
};

const copyLink = async () => {
	try {
		await navigator.clipboard.writeText(props.url);
		copied.value = true;
		setTimeout(() => {
			copied.value = false;
		}, 2000);
	} catch (err) {
		// Fallback for older browsers
		const textArea = document.createElement("textarea");
		textArea.value = props.url;
		document.body.appendChild(textArea);
		textArea.select();
		document.execCommand("copy");
		document.body.removeChild(textArea);
		copied.value = true;
		setTimeout(() => {
			copied.value = false;
		}, 2000);
	}
};
</script>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'SocialShare'
})
</script>