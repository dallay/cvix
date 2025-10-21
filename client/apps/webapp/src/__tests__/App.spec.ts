import { mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { beforeEach, describe, expect, it } from "vitest";
import { createRouter, createWebHistory } from "vue-router";
import App from "../App.vue";

describe("App", () => {
	let pinia: ReturnType<typeof createPinia>;
	let router: ReturnType<typeof createRouter>;

	beforeEach(() => {
		pinia = createPinia();
		router = createRouter({
			history: createWebHistory(),
			routes: [
				{
					path: "/",
					name: "home",
					component: { template: "<div>Home</div>" },
				},
			],
		});
	});

	it("mounts renders properly", async () => {
		const wrapper = mount(App, {
			global: {
				plugins: [pinia, router],
				stubs: {
					RouterView: { template: "<div>You did it!</div>" },
				},
			},
		});

		await wrapper.vm.$nextTick();
		expect(wrapper.text()).toContain("You did it!");
	});
});
