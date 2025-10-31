<script setup lang="ts">
import { toTypedSchema } from "@vee-validate/zod";
import { useForm } from "vee-validate";
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
	FormControl,
	FormDescription,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
	type RegisterFormData,
	registerSchema,
} from "../../domain/validators/auth.schema.ts";
import { useAuthStore } from "../stores/authStore.ts";

const router = useRouter();
const authStore = useAuthStore();

const isSubmitting = ref(false);
const submitError = ref<string | null>(null);

const { handleSubmit, errors } = useForm<RegisterFormData>({
	validationSchema: toTypedSchema(registerSchema),
	initialValues: {
		firstName: "",
		lastName: "",
		email: "",
		password: "",
		confirmPassword: "",
		acceptTerms: false,
	},
});

const onSubmit = handleSubmit(async (values) => {
	isSubmitting.value = true;
	submitError.value = null;

	try {
		await authStore.register(values);
		await router.push("/login");
	} catch (error) {
		submitError.value =
			error instanceof Error
				? error.message
				: "Registration failed. Please try again.";
	} finally {
		isSubmitting.value = false;
	}
});
</script>

<template>
  <div class="w-full max-w-md mx-auto space-y-6">
    <div class="space-y-2 text-center">
      <h1 class="text-3xl font-bold">Create your account</h1>
      <p class="text-muted-foreground">
        Enter your information to create an account
      </p>
    </div>

    <form @submit="onSubmit" class="space-y-4">
      <!-- First Name -->
      <FormField
        v-slot="{ componentField }"
        name="firstName"
        validate-on-blur
        :validate-on-change="false"
        :validate-on-input="false"
      >
        <FormItem>
          <FormLabel>First Name</FormLabel>
          <FormControl>
            <Input
              type="text"
              placeholder="John"
              v-bind="componentField"
              :disabled="isSubmitting"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <!-- Last Name -->
      <FormField
        v-slot="{ componentField }"
        name="lastName"
        validate-on-blur
        :validate-on-change="false"
        :validate-on-input="false"
      >
        <FormItem>
          <FormLabel>Last Name</FormLabel>
          <FormControl>
            <Input
              type="text"
              placeholder="Doe"
              v-bind="componentField"
              :disabled="isSubmitting"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <!-- Email -->
      <FormField
        v-slot="{ componentField }"
        name="email"
        validate-on-blur
        :validate-on-change="false"
        :validate-on-input="false"
      >
        <FormItem>
          <FormLabel>Email</FormLabel>
          <FormControl>
            <Input
              type="email"
              placeholder="john.doe@example.com"
              v-bind="componentField"
              :disabled="isSubmitting"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <!-- Password -->
      <FormField
        v-slot="{ componentField }"
        name="password"
        validate-on-blur
        :validate-on-change="false"
        :validate-on-input="false"
      >
        <FormItem>
          <FormLabel>Password</FormLabel>
          <FormControl>
            <Input
              type="password"
              placeholder="••••••••"
              v-bind="componentField"
              :disabled="isSubmitting"
            />
          </FormControl>
          <FormDescription>
            Must be at least 8 characters with uppercase, lowercase, number, and special character
          </FormDescription>
          <FormMessage />
        </FormItem>
      </FormField>

      <!-- Confirm Password -->
      <FormField
        v-slot="{ componentField }"
        name="confirmPassword"
        validate-on-blur
        validate-on-change
        :validate-on-input="false"
      >
        <FormItem>
          <FormLabel>Confirm Password</FormLabel>
          <FormControl>
            <Input
              type="password"
              placeholder="••••••••"
              v-bind="componentField"
              :disabled="isSubmitting"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <!-- Accept Terms -->
      <FormField v-slot="{ componentField }" name="acceptTerms" type="checkbox">
        <FormItem class="flex flex-row items-start space-x-3 space-y-0">
          <FormControl>
            <Checkbox
              v-bind="componentField"
              :disabled="isSubmitting"
            />
          </FormControl>
          <div class="space-y-1 leading-none">
            <FormLabel>
              I accept the <a href="/terms" class="underline hover:text-primary">terms and conditions</a>
            </FormLabel>
            <FormMessage />
          </div>
        </FormItem>
      </FormField>

      <!-- Error Alert -->
      <Alert v-if="submitError" variant="destructive">
        <AlertDescription>{{ submitError }}</AlertDescription>
      </Alert>

      <!-- Submit Button -->
      <Button
        type="submit"
        class="w-full"
        :disabled="isSubmitting"
      >
        {{ isSubmitting ? 'Creating account...' : 'Create account' }}
      </Button>

      <!-- Login Link -->
      <p class="text-center text-sm text-muted-foreground">
        Already have an account?
        <router-link to="/login" class="underline hover:text-primary">
          Sign in
        </router-link>
      </p>
    </form>
  </div>
</template>
