<template>
  <div class="relative w-full max-w-lg">
    <div class="relative">
      <Icon name="tabler:search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
      <input
        v-model="searchQuery"
        type="text"
        :placeholder="t('blog.search.placeholder')"
        class="w-full pl-10 pr-4 py-2 border border-input rounded-md bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent"
        @input="handleSearch"
        @keydown.escape="clearSearch"
        role="searchbox"
        :aria-label="t('blog.search.placeholder')"
      />
      <button
        v-if="searchQuery"
        @click="clearSearch"
        class="absolute right-3 top-1/2 transform -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
        :aria-label="t('blog.search.clear')"
      >
        <Icon name="tabler:x" class="w-4 h-4" />
      </button>
    </div>
    
    <div v-if="searchQuery && searchResults !== null" class="mt-2 text-sm text-muted-foreground">
      {{ searchResults.length > 0 
        ? t('blog.search.results', { count: searchResults.length }) 
        : t('blog.search.noResults') 
      }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon } from "astro-icon/components";
import { computed, ref, watch } from "vue";
import { type Lang, useTranslations } from "@/i18n";
import type Article from "@/models/article/article.model";

interface Props {
	articles: Article[];
	lang: Lang;
}

type Emits = (e: "search", results: Article[]) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const t = useTranslations(props.lang);

const searchQuery = ref("");
const searchResults = ref<Article[] | null>(null);

const handleSearch = () => {
	if (!searchQuery.value.trim()) {
		searchResults.value = null;
		emit("search", props.articles);
		return;
	}

	const query = searchQuery.value.toLowerCase();
	const results = props.articles.filter(
		(article) =>
			article.title.toLowerCase().includes(query) ||
			article.description?.toLowerCase().includes(query) ||
			article.category?.title?.toLowerCase().includes(query) ||
			article.tags?.some((tag) => tag.title?.toLowerCase().includes(query)),
	);

	searchResults.value = results;
	emit("search", results);
};

const clearSearch = () => {
	searchQuery.value = "";
	searchResults.value = null;
	emit("search", props.articles);
};

// Watch for external changes to articles
watch(
	() => props.articles,
	() => {
		if (searchQuery.value) {
			handleSearch();
		}
	},
);
</script>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'BlogSearch'
})
</script>