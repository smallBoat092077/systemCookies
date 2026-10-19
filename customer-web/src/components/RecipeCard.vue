<template>
  <div class="recipe-card" @click="emit('click', recipe)">
    <van-image v-if="recipe.coverUrl" class="cover" :src="recipe.coverUrl" fit="cover">
      <template #error>
        <div class="cover-fallback">{{ recipe.name?.slice(0, 1) || '菜' }}</div>
      </template>
    </van-image>
    <div v-else class="cover-fallback">{{ recipe.name?.slice(0, 1) || '菜' }}</div>

    <div class="info">
      <div class="name-row">
        <span class="name">{{ recipe.name }}</span>
        <van-tag v-if="recipe.isRecommend === 1" type="danger" size="mini">招牌</van-tag>
      </div>
      <div class="subtitle">{{ recipe.subtitle }}</div>

      <div class="tags">
        <van-tag v-if="recipe.taste" plain type="primary">{{ recipe.taste }}</van-tag>
        <van-tag v-if="recipe.cookMethod" plain type="success">{{ recipe.cookMethod }}</van-tag>
        <van-tag v-if="recipe.cookTime" plain>{{ recipe.cookTime }} 分钟</van-tag>
        <van-tag v-if="recipe.stepCount" plain type="warning">{{ recipe.stepCount }} 步做法</van-tag>
      </div>

      <div class="bottom">
        <span class="price">¥{{ recipe.price }}</span>
        <span v-if="recipe.originalPrice" class="origin">¥{{ recipe.originalPrice }}</span>
        <span class="sales">已售 {{ recipe.sales ?? 0 }}</span>
        <van-button
          class="add-btn"
          icon="plus"
          type="primary"
          size="small"
          round
          @click.stop="emit('add', recipe)"
        >
          加购
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  recipe: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'add'])
</script>

<style scoped>
.recipe-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin: 0 12px 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.cover,
.cover-fallback {
  width: 96px;
  height: 96px;
  flex: none;
  border-radius: 8px;
  overflow: hidden;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.subtitle {
  font-size: 12px;
  color: #969799;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.bottom {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: auto;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ee0a24;
}

.origin {
  font-size: 12px;
  color: #c8c9cc;
  text-decoration: line-through;
}

.sales {
  margin-left: auto;
  font-size: 12px;
  color: #969799;
}
</style>
