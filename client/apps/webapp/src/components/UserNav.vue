<script setup lang="ts">
import { Avatar, AvatarFallback } from "@cvix/ui/components/ui/avatar";
import { Button } from "@cvix/ui/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuGroup,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@cvix/ui/components/ui/dropdown-menu";
import { storeToRefs } from "pinia";
import { ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { useAuthStore } from "@/core/authentication/presentation/stores/authStore";
import PhGearLight from "~icons/ph/gear-light";
import PhSignOutLight from "~icons/ph/sign-out-light";
import PhUserLight from "~icons/ph/user-light";

interface Props {
	variant?: "default" | "compact";
}

withDefaults(defineProps<Props>(), {
	variant: "default",
});

const router = useRouter();
const authStore = useAuthStore();
const { user } = storeToRefs(authStore);
const isLoggingOut = ref(false);

const handleLogout = async () => {
	if (isLoggingOut.value) return;

	isLoggingOut.value = true;
	try {
		await authStore.logout();
		toast.success("Successfully logged out");
		await router.push("/login");
	} catch (error) {
		toast.error("Logout failed");
	} finally {
		isLoggingOut.value = false;
	}
};

// Generate user initials from first and last name or username
const getUserInitials = (currentUser: typeof user.value) => {
	if (!currentUser) return "?";

	if (currentUser.firstName && currentUser.lastName) {
		return `${currentUser.firstName.charAt(0)}${currentUser.lastName.charAt(0)}`.toUpperCase();
	}

	if (currentUser.firstName) {
		return currentUser.firstName.charAt(0).toUpperCase();
	}

	return currentUser.username.charAt(0).toUpperCase();
};

// Get display name
const getDisplayName = (currentUser: typeof user.value) => {
	if (!currentUser) return "User";

	if (currentUser.firstName && currentUser.lastName) {
		return `${currentUser.firstName} ${currentUser.lastName}`;
	}

	if (currentUser.firstName) {
		return currentUser.firstName;
	}

	return currentUser.username;
};
</script>

<template>
  <DropdownMenu v-if="user">
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" class="relative h-8 w-8 rounded-full">
        <Avatar class="h-8 w-8">
          <AvatarFallback>{{ getUserInitials(user) }}</AvatarFallback>
        </Avatar>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent class="w-56" align="end">
      <DropdownMenuLabel class="font-normal">
        <div class="flex flex-col space-y-1">
          <p class="text-sm font-medium leading-none">
            {{ getDisplayName(user) }}
          </p>
          <p class="text-xs leading-none text-muted-foreground">
            {{ user.email }}
          </p>
        </div>
      </DropdownMenuLabel>
      <DropdownMenuSeparator />
      <DropdownMenuGroup>
        <DropdownMenuItem @click="router.push('/profile')">
          <PhUserLight class="mr-2 h-4 w-4" />
          <span>Profile</span>
        </DropdownMenuItem>
        <DropdownMenuItem @click="router.push('/settings')">
          <PhGearLight class="mr-2 h-4 w-4" />
          <span>Settings</span>
        </DropdownMenuItem>
      </DropdownMenuGroup>
      <DropdownMenuSeparator />
      <DropdownMenuItem @click="handleLogout" :disabled="isLoggingOut">
        <PhSignOutLight class="mr-2 h-4 w-4" />
        <span>{{ isLoggingOut ? "Logging out..." : "Log out" }}</span>
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
  <Button v-else variant="ghost" size="sm" @click="router.push('/login')">
    Sign In
  </Button>
</template>
