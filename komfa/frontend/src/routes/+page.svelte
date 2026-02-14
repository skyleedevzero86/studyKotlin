<script lang="ts">
	import { createCounter } from '$lib/domain/counter/index.js';
	import { createUseCounter } from '$lib/application/counter/index.js';

	let counter = $state(createCounter(0));
	const store = {
		get: () => counter,
		set: (c: ReturnType<typeof createCounter>) => {
			counter = c;
		}
	};
	const actions = createUseCounter(store);
</script>

<div class="container">
	<h1>DDD + Clean + Functional (Svelte 5)</h1>
	<p class="count">Count: {counter.value}</p>
	<div class="actions">
		<button onclick={() => actions.dec()}>−</button>
		<button onclick={() => actions.inc()}>+</button>
		<button onclick={() => actions.addBy(10)}>+10</button>
	</div>
</div>

<style>
	.container {
		max-width: 40rem;
		margin: 2rem auto;
		text-align: center;
		font-family: system-ui, sans-serif;
	}
	.count {
		font-size: 2rem;
		font-weight: 600;
		margin: 1rem 0;
	}
	.actions {
		display: flex;
		gap: 0.5rem;
		justify-content: center;
	}
	button {
		padding: 0.5rem 1rem;
		font-size: 1rem;
		cursor: pointer;
	}
</style>
