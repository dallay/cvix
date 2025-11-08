<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();

const goToDashboard = () => {
	router.push("/dashboard");
};

const goToLogin = () => {
	router.push("/login");
};
</script>

<template>
	<div class="min-h-screen flex items-center justify-center bg-background">
		<div class="max-w-md w-full space-y-8 p-8">
			<div class="text-center">
				<svg
					class="mx-auto h-24 w-24 text-destructive"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
					/>
				</svg>
				<h2 class="mt-6 text-3xl font-extrabold text-foreground">
					Access Denied
				</h2>
				<p class="mt-2 text-sm text-muted-foreground">
					You don't have permission to access this page.
				</p>
				<p class="mt-1 text-xs text-muted-foreground">
					Please contact your administrator if you believe you should have
					access.
				</p>
			</div>

			<div class="flex flex-col gap-3">
				<button
					type="button"
					class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-primary-foreground bg-primary hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-ring"
					@click="goToDashboard"
				>
					Go to Dashboard
				</button>
				<button
					type="button"
					class="w-full flex justify-center py-2 px-4 border border-input rounded-md shadow-sm text-sm font-medium text-foreground bg-background hover:bg-accent focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-ring"
					@click="goToLogin"
				>
					Back to Login
				</button>
			</div>
		</div>
	</div>
</template>

