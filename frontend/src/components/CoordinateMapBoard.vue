<template>
  <div class="coordinate-map-board">
    <div
      ref="boardRef"
      class="coordinate-map-board__surface"
      :class="{ 'coordinate-map-board__surface--interactive': interactive }"
      :style="surfaceStyle"
      @click="handleSurfaceClick"
    >
      <div class="coordinate-map-board__grid" />
      <div class="coordinate-map-board__axis coordinate-map-board__axis--top">
        <span>180W</span>
        <span>0</span>
        <span>180E</span>
      </div>
      <div class="coordinate-map-board__axis coordinate-map-board__axis--left">
        <span>90N</span>
        <span>0</span>
        <span>90S</span>
      </div>

      <button
        v-for="point in normalizedPoints"
        :key="point.id ?? `${point.longitude}-${point.latitude}`"
        type="button"
        class="coordinate-map-board__point"
        :class="{ 'coordinate-map-board__point--active': selectedId != null && point.id === selectedId }"
        :style="{ left: `${point.x}%`, top: `${point.y}%` }"
        @click.stop="handlePointClick(point)"
      >
        <span class="coordinate-map-board__point-dot" />
        <span class="coordinate-map-board__point-label">{{ point.label || point.regionName || point.regionCode || fallbackPointLabel }}</span>
      </button>

      <div
        v-if="interactive && currentCoordinate"
        class="coordinate-map-board__cursor"
        :style="{ left: `${currentCoordinate.x}%`, top: `${currentCoordinate.y}%` }"
      />

      <div v-if="!normalizedPoints.length && !currentCoordinate" class="coordinate-map-board__empty">
        {{ emptyText }}
      </div>
    </div>

    <div class="coordinate-map-board__footer">
      <span>{{ interactive ? interactiveHint : readonlyHint }}</span>
      <span v-if="coordinateSummary">{{ currentLabelPrefix }}{{ coordinateSummary }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  points: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: () => ({ longitude: null, latitude: null })
  },
  interactive: {
    type: Boolean,
    default: false
  },
  selectedId: {
    type: [Number, String],
    default: null
  },
  height: {
    type: Number,
    default: 320
  },
  emptyText: {
    type: String,
    default: '\u6682\u65e0\u53ef\u5c55\u793a\u5750\u6807'
  }
})

const emit = defineEmits(['update:modelValue', 'pick'])
const boardRef = ref(null)
const interactiveHint = '\u70b9\u51fb\u5750\u6807\u9762\u677f\u53ef\u9009\u70b9\u5e76\u81ea\u52a8\u56de\u586b\u7ecf\u7eac\u5ea6'
const readonlyHint = '\u5c55\u793a\u5df2\u914d\u7f6e\u7ecf\u7eac\u5ea6\u7684\u533a\u57df\u70b9\u4f4d'
const currentLabelPrefix = '\u5f53\u524d\u5750\u6807\uff1a'
const fallbackPointLabel = '\u70b9\u4f4d'

const surfaceStyle = computed(() => ({ height: `${props.height}px` }))

const normalizedPoints = computed(() => {
  return props.points
    .filter((item) => Number.isFinite(Number(item?.longitude)) && Number.isFinite(Number(item?.latitude)))
    .map((item) => ({
      ...item,
      x: toX(Number(item.longitude)),
      y: toY(Number(item.latitude))
    }))
})

const currentCoordinate = computed(() => {
  const longitude = Number(props.modelValue?.longitude)
  const latitude = Number(props.modelValue?.latitude)
  if (!Number.isFinite(longitude) || !Number.isFinite(latitude)) {
    return null
  }
  return {
    longitude,
    latitude,
    x: toX(longitude),
    y: toY(latitude)
  }
})

const coordinateSummary = computed(() => {
  if (!currentCoordinate.value) {
    return ''
  }
  return `${currentCoordinate.value.longitude.toFixed(6)}, ${currentCoordinate.value.latitude.toFixed(6)}`
})

function toX(longitude) {
  return ((longitude + 180) / 360) * 100
}

function toY(latitude) {
  return ((90 - latitude) / 180) * 100
}

function toLongitude(x) {
  return Number((((x / 100) * 360) - 180).toFixed(6))
}

function toLatitude(y) {
  return Number((90 - ((y / 100) * 180)).toFixed(6))
}

function handleSurfaceClick(event) {
  if (!props.interactive || !boardRef.value) {
    return
  }
  const rect = boardRef.value.getBoundingClientRect()
  const percentX = Math.min(100, Math.max(0, ((event.clientX - rect.left) / rect.width) * 100))
  const percentY = Math.min(100, Math.max(0, ((event.clientY - rect.top) / rect.height) * 100))
  const coordinate = {
    longitude: toLongitude(percentX),
    latitude: toLatitude(percentY)
  }
  emit('update:modelValue', coordinate)
  emit('pick', coordinate)
}

function handlePointClick(point) {
  emit('pick', point)
  if (props.interactive) {
    emit('update:modelValue', {
      longitude: Number(point.longitude),
      latitude: Number(point.latitude)
    })
  }
}
</script>

<style scoped>
.coordinate-map-board {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coordinate-map-board__surface {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(36, 94, 122, 0.12);
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(255, 207, 112, 0.45), transparent 38%),
    radial-gradient(circle at bottom right, rgba(78, 156, 168, 0.22), transparent 42%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(241, 248, 250, 0.98));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.coordinate-map-board__surface--interactive {
  cursor: crosshair;
}

.coordinate-map-board__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(37, 92, 120, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 92, 120, 0.08) 1px, transparent 1px);
  background-size: calc(100% / 6) calc(100% / 6);
}

.coordinate-map-board__axis {
  position: absolute;
  display: flex;
  color: #5f7381;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.coordinate-map-board__axis--top {
  top: 12px;
  left: 20px;
  right: 20px;
  justify-content: space-between;
}

.coordinate-map-board__axis--left {
  top: 42px;
  bottom: 18px;
  left: 16px;
  flex-direction: column;
  justify-content: space-between;
}

.coordinate-map-board__point {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  border: none;
  background: transparent;
  transform: translate(-50%, -50%);
  cursor: pointer;
}

.coordinate-map-board__point-dot {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 3px solid rgba(255, 255, 255, 0.95);
  background: #1d8f89;
  box-shadow: 0 10px 20px rgba(29, 143, 137, 0.28);
}

.coordinate-map-board__point--active .coordinate-map-board__point-dot {
  background: #ef8c28;
  box-shadow: 0 14px 28px rgba(239, 140, 40, 0.32);
}

.coordinate-map-board__point-label {
  max-width: 180px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #193446;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 8px 18px rgba(25, 52, 70, 0.12);
}

.coordinate-map-board__cursor {
  position: absolute;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(235, 108, 46, 0.9);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 8px rgba(235, 108, 46, 0.12);
}

.coordinate-map-board__empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b8391;
  font-size: 14px;
}

.coordinate-map-board__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #627884;
  font-size: 13px;
}

@media (max-width: 960px) {
  .coordinate-map-board__footer {
    flex-direction: column;
  }

  .coordinate-map-board__point-label {
    max-width: 120px;
  }
}
</style>
