<script setup lang="ts">
/**
 * ResumePreviewSkeleton Component
 *
 * Displays a skeleton preview that simulates a resume structure while
 * waiting for the actual PDF to be generated. Shows:
 * - Header with avatar placeholder and name/title lines
 * - Multiple sections with headings and content lines
 * - Proper spacing to match A4 proportions
 */
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <div
    class="rounded-lg shadow-2xl border border-border bg-white overflow-hidden"
    style="width: 210mm; height: 297mm;"
    data-testid="resume-preview-skeleton"
  >
    <!-- Skeleton Content -->
    <div class="p-8 animate-pulse">
      <!-- Header Section -->
      <div class="flex items-start gap-6 mb-8">
        <!-- Avatar Placeholder -->
        <div class="w-20 h-20 rounded-full bg-muted shrink-0" />

        <!-- Name & Title -->
        <div class="flex-1 space-y-3 pt-2">
          <div class="h-7 bg-muted rounded-md w-2/3" />
          <div class="h-4 bg-muted/70 rounded w-1/2" />
          <div class="flex gap-4 mt-3">
            <div class="h-3 bg-muted/50 rounded w-24" />
            <div class="h-3 bg-muted/50 rounded w-32" />
            <div class="h-3 bg-muted/50 rounded w-28" />
          </div>
        </div>
      </div>

      <!-- Divider -->
      <div class="h-px bg-muted/60 mb-8" />

      <!-- Summary Section -->
      <div class="mb-8">
        <div class="h-4 bg-primary/20 rounded w-24 mb-4" />
        <div class="space-y-2">
          <div class="h-3 bg-muted/40 rounded w-full" />
          <div class="h-3 bg-muted/40 rounded w-full" />
          <div class="h-3 bg-muted/40 rounded w-4/5" />
        </div>
      </div>

      <!-- Experience Section -->
      <div class="mb-8">
        <div class="h-4 bg-primary/20 rounded w-32 mb-4" />

        <!-- Experience Item 1 -->
        <div class="mb-6">
          <div class="flex justify-between items-start mb-2">
            <div class="space-y-1.5">
              <div class="h-4 bg-muted/60 rounded w-48" />
              <div class="h-3 bg-muted/40 rounded w-36" />
            </div>
            <div class="h-3 bg-muted/30 rounded w-28" />
          </div>
          <div class="space-y-1.5 mt-3">
            <div class="h-2.5 bg-muted/30 rounded w-full" />
            <div class="h-2.5 bg-muted/30 rounded w-full" />
            <div class="h-2.5 bg-muted/30 rounded w-3/4" />
          </div>
        </div>

        <!-- Experience Item 2 -->
        <div class="mb-6">
          <div class="flex justify-between items-start mb-2">
            <div class="space-y-1.5">
              <div class="h-4 bg-muted/60 rounded w-44" />
              <div class="h-3 bg-muted/40 rounded w-32" />
            </div>
            <div class="h-3 bg-muted/30 rounded w-24" />
          </div>
          <div class="space-y-1.5 mt-3">
            <div class="h-2.5 bg-muted/30 rounded w-full" />
            <div class="h-2.5 bg-muted/30 rounded w-5/6" />
          </div>
        </div>
      </div>

      <!-- Education Section -->
      <div class="mb-8">
        <div class="h-4 bg-primary/20 rounded w-28 mb-4" />

        <div class="flex justify-between items-start mb-2">
          <div class="space-y-1.5">
            <div class="h-4 bg-muted/60 rounded w-52" />
            <div class="h-3 bg-muted/40 rounded w-40" />
          </div>
          <div class="h-3 bg-muted/30 rounded w-20" />
        </div>
      </div>

      <!-- Skills Section -->
      <div class="mb-8">
        <div class="h-4 bg-primary/20 rounded w-20 mb-4" />
        <div class="flex flex-wrap gap-2">
          <div class="h-6 bg-muted/30 rounded-full w-20" />
          <div class="h-6 bg-muted/30 rounded-full w-24" />
          <div class="h-6 bg-muted/30 rounded-full w-16" />
          <div class="h-6 bg-muted/30 rounded-full w-28" />
          <div class="h-6 bg-muted/30 rounded-full w-20" />
          <div class="h-6 bg-muted/30 rounded-full w-22" />
        </div>
      </div>

      <!-- Languages Section -->
      <div>
        <div class="h-4 bg-primary/20 rounded w-24 mb-4" />
        <div class="flex gap-6">
          <div class="flex items-center gap-2">
            <div class="h-3 bg-muted/40 rounded w-16" />
            <div class="h-2 bg-muted/20 rounded w-12" />
          </div>
          <div class="flex items-center gap-2">
            <div class="h-3 bg-muted/40 rounded w-14" />
            <div class="h-2 bg-muted/20 rounded w-16" />
          </div>
        </div>
      </div>
    </div>

    <!-- Footer hint -->
    <div class="absolute bottom-8 left-0 right-0 flex justify-center">
      <p class="text-xs text-muted-foreground/50">
        {{ t('resume.pdfPage.skeletonHint', 'Your PDF preview will appear here') }}
      </p>
    </div>
  </div>
</template>
