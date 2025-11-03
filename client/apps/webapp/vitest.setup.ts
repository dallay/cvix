import { expect } from "vitest";
import * as matchers from "vitest-axe/matchers";

// Extend Vitest expect with axe accessibility matchers
expect.extend(matchers);
