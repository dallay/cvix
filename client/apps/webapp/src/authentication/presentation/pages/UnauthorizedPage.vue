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
	<div class="min-h-screen flex items-center justify-center bg-gray-50">
		<div class="max-w-md w-full space-y-8 p-8">
			<div class="text-center">
				<svg
					class="mx-auto h-24 w-24 text-red-500"
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
				<h2 class="mt-6 text-3xl font-extrabold text-gray-900">
					Access Denied
				</h2>
				<p class="mt-2 text-sm text-gray-600">
					You don't have permission to access this page.
				</p>
				<p class="mt-1 text-xs text-gray-500">
					Please contact your administrator if you believe you should have
					access.
				</p>
			</div>

			<div class="flex flex-col gap-3">
				<button
					type="button"
					class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
					@click="goToDashboard"
				>
					Go to Dashboard
				</button>
				<button
					type="button"
					class="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
					@click="goToLogin"
				>
					Back to Login
				</button>
			</div>
		</div>
	</div>
</template>

