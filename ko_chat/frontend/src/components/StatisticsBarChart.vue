<script setup lang="ts">
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  Legend,
  LinearScale,
  Title,
  Tooltip,
  type ChartData,
} from 'chart.js'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

Chart.register(CategoryScale, LinearScale, BarElement, BarController, Title, Tooltip, Legend)

export interface ChartDataset {
  label: string
  values: number[]
  color?: string
}

const props = withDefaults(
  defineProps<{
    labels: string[]
    values?: number[]
    datasets?: ChartDataset[]
    title?: string
    color?: string
    stacked?: boolean
  }>(),
  {
    values: () => [],
    datasets: () => [],
    stacked: false,
  },
)

const DEFAULT_COLORS = [
  'rgba(79, 70, 229, 0.8)',
  'rgba(16, 185, 129, 0.8)',
  'rgba(245, 158, 11, 0.8)',
  'rgba(239, 68, 68, 0.8)',
  'rgba(59, 130, 246, 0.8)',
  'rgba(168, 85, 247, 0.8)',
]

const canvasRef = ref<HTMLCanvasElement | null>(null)
const wrapRef = ref<HTMLDivElement | null>(null)
let chart: Chart | null = null
let resizeObserver: ResizeObserver | null = null

const buildChartData = (): ChartData<'bar'> => {
  const series =
    props.datasets.length > 0
      ? props.datasets
      : [
          {
            label: props.title ?? '건수',
            values: props.values,
            color: props.color ?? DEFAULT_COLORS[0],
          },
        ]

  return {
    labels: props.labels,
    datasets: series.map((item, index) => {
      const color = item.color ?? DEFAULT_COLORS[index % DEFAULT_COLORS.length]
      return {
        label: item.label,
        data: item.values,
        backgroundColor: color,
        borderColor: color.replace('0.8', '1'),
        borderWidth: 1,
        borderRadius: 4,
      }
    }),
  }
}

const renderChart = async () => {
  await nextTick()
  if (!canvasRef.value || !props.labels.length) {
    chart?.destroy()
    chart = null
    return
  }

  const data = buildChartData()
  if (chart) {
    chart.data = data
    chart.options.plugins!.legend!.display = data.datasets.length > 1 || Boolean(props.title)
    chart.update()
    return
  }

  chart = new Chart(canvasRef.value, {
    type: 'bar',
    data,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: data.datasets.length > 1 || Boolean(props.title),
          position: 'top',
        },
        tooltip: {
          mode: props.stacked ? 'index' : 'nearest',
          intersect: false,
        },
      },
      scales: {
        x: {
          stacked: props.stacked,
          ticks: {
            maxRotation: 45,
            minRotation: 0,
            autoSkip: true,
            maxTicksLimit: 24,
          },
        },
        y: {
          stacked: props.stacked,
          beginAtZero: true,
          ticks: { precision: 0 },
        },
      },
    },
  })
}

onMounted(() => {
  void renderChart()
  if (wrapRef.value && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      chart?.resize()
    })
    resizeObserver.observe(wrapRef.value)
  }
})

watch(
  () => [props.labels, props.values, props.datasets, props.title, props.stacked],
  () => {
    void renderChart()
  },
  { deep: true },
)

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chart?.destroy()
  chart = null
})
</script>

<template>
  <div ref="wrapRef" class="stats-chart-wrap">
    <p v-if="!labels.length" class="stats-chart-empty">표시할 차트 데이터가 없습니다.</p>
    <canvas v-show="labels.length" ref="canvasRef" />
  </div>
</template>

<style scoped src="../styles/components/StatisticsBarChart.css"></style>
