<template>
  <div class="page">
    <el-card v-loading="loading" shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="菜名" prop="name">
              <el-input v-model="form.name" placeholder="如：番茄炒蛋" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="副标题">
              <el-input v-model="form.subtitle" placeholder="一句话卖点" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="菜系">
              <el-input v-model="form.cuisine" placeholder="如：川菜 / 家常菜" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="口味">
              <el-input v-model="form.taste" placeholder="如：酸甜" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主要做法">
              <el-input v-model="form.cookMethod" placeholder="如：炒 / 炖" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="难度">
              <el-radio-group v-model="form.difficulty">
                <el-radio :value="1">简单</el-radio>
                <el-radio :value="2">中等</el-radio>
                <el-radio :value="3">困难</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总耗时(分钟)">
              <el-input-number v-model="form.cookTime" :min="0" :max="1440" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="份量(人份)">
              <el-input-number v-model="form.servings" :min="1" :max="50" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可售库存">
              <el-input-number v-model="form.stock" :min="0" :max="99999" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="热量(千卡)">
              <el-input-number v-model="form.calories" :min="0" :max="9999" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="招牌推荐">
              <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="上架状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>

          <el-col :span="16">
            <el-form-item label="封面图">
              <el-input v-model="form.coverUrl" placeholder="图片 URL" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜品简介">
              <el-input v-model="form.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 烹饪步骤 -->
        <el-divider content-position="left">
          <span>烹饪步骤</span>
          <el-button class="divider-btn" size="small" type="primary" plain @click="addStep">
            添加步骤
          </el-button>
        </el-divider>
        <div v-for="(step, index) in form.steps" :key="index" class="sub-row">
          <div class="sub-head">
            <el-tag type="warning">第 {{ index + 1 }} 步</el-tag>
            <el-input v-model="step.title" class="sub-title" placeholder="步骤标题，如：备料" />
            <el-input-number v-model="step.duration" :min="0" :max="600" size="small" placeholder="耗时" />
            <span class="unit">分钟</span>
            <el-button link type="danger" @click="removeRow(form.steps, index)">删除</el-button>
          </div>
          <el-input v-model="step.content" type="textarea" :rows="2" placeholder="这一步具体怎么做" />
          <div class="sub-extra">
            <el-input v-model="step.imageUrl" placeholder="步骤配图 URL（可选）" />
            <el-input v-model="step.tip" placeholder="本步要点（可选）" />
          </div>
        </div>
        <el-empty v-if="!form.steps.length" description="还没有步骤，点击右上角「添加步骤」" :image-size="60" />

        <!-- 食材 -->
        <el-divider content-position="left">
          <span>食材清单</span>
          <el-button class="divider-btn" size="small" type="primary" plain @click="addIngredient">
            添加食材
          </el-button>
        </el-divider>
        <div v-for="(item, index) in form.ingredients" :key="index" class="sub-row inline">
          <el-input v-model="item.name" placeholder="食材名" />
          <el-input v-model="item.amount" placeholder="用量" />
          <el-input v-model="item.unit" placeholder="单位" />
          <el-select v-model="item.isMain" style="width: 100px">
            <el-option label="主料" :value="1" />
            <el-option label="辅料" :value="0" />
          </el-select>
          <el-input v-model="item.note" placeholder="备注（可选）" />
          <el-button link type="danger" @click="removeRow(form.ingredients, index)">删除</el-button>
        </div>
        <el-empty v-if="!form.ingredients.length" description="还没有食材" :image-size="60" />

        <!-- 调料 -->
        <el-divider content-position="left">
          <span>调料清单</span>
          <el-button class="divider-btn" size="small" type="primary" plain @click="addSeasoning">
            添加调料
          </el-button>
        </el-divider>
        <div v-for="(item, index) in form.seasonings" :key="index" class="sub-row inline">
          <el-input v-model="item.name" placeholder="调料名" />
          <el-input v-model="item.amount" placeholder="用量" />
          <el-input v-model="item.unit" placeholder="单位" />
          <el-input v-model="item.note" placeholder="备注（可选）" />
          <el-button link type="danger" @click="removeRow(form.seasonings, index)">删除</el-button>
        </div>
        <el-empty v-if="!form.seasonings.length" description="还没有调料" :image-size="60" />

        <!-- 烹饪技巧 -->
        <el-divider content-position="left">
          <span>烹饪技巧</span>
          <el-button class="divider-btn" size="small" type="primary" plain @click="addTip">
            添加技巧
          </el-button>
        </el-divider>
        <div v-for="(item, index) in form.tips" :key="index" class="sub-row">
          <div class="sub-head">
            <el-tag type="success">技巧 {{ index + 1 }}</el-tag>
            <el-input v-model="item.title" class="sub-title" placeholder="技巧标题" />
            <el-button link type="danger" @click="removeRow(form.tips, index)">删除</el-button>
          </div>
          <el-input v-model="item.content" type="textarea" :rows="2" placeholder="技巧说明" />
        </div>
        <el-empty v-if="!form.tips.length" description="还没有烹饪技巧" :image-size="60" />

        <div class="footer">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存菜谱</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchRecipeDetail, createRecipe, updateRecipe } from '@/api/recipe'
import { fetchCategoryList } from '@/api/category'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const categories = ref([])

const createForm = () => ({
  id: null,
  categoryId: undefined,
  name: '',
  subtitle: '',
  coverUrl: '',
  description: '',
  cuisine: '',
  taste: '',
  cookMethod: '',
  difficulty: 1,
  cookTime: 10,
  servings: 2,
  calories: 0,
  price: 0,
  originalPrice: 0,
  stock: 0,
  isRecommend: 0,
  status: 1,
  steps: [],
  ingredients: [],
  seasonings: [],
  tips: []
})

const form = reactive(createForm())

const rules = {
  name: [{ required: true, message: '请输入菜名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const addStep = () =>
  form.steps.push({ stepNo: form.steps.length + 1, title: '', content: '', imageUrl: '', duration: 0, tip: '' })
const addIngredient = () => form.ingredients.push({ name: '', amount: '', unit: '', isMain: 0, note: '' })
const addSeasoning = () => form.seasonings.push({ name: '', amount: '', unit: '', note: '' })
const addTip = () => form.tips.push({ title: '', content: '' })

const removeRow = (arr, index) => arr.splice(index, 1)

const goBack = () => router.push('/recipes')

const buildPayload = () => {
  const payload = JSON.parse(JSON.stringify(form))
  payload.steps = payload.steps.map((item, index) => ({
    ...item,
    stepNo: index + 1,
    duration: item.duration ?? 0
  }))
  payload.ingredients = payload.ingredients.map((item, index) => ({
    ...item,
    isMain: item.isMain ?? 0,
    sort: index
  }))
  payload.seasonings = payload.seasonings.map((item, index) => ({ ...item, sort: index }))
  payload.tips = payload.tips.map((item, index) => ({ ...item, sort: index }))
  if (!payload.id) {
    delete payload.id
  }
  return payload
}

const validateChildren = () => {
  for (let i = 0; i < form.steps.length; i++) {
    if (!form.steps[i].content?.trim()) {
      ElMessage.warning(`第 ${i + 1} 步的做法内容不能为空`)
      return false
    }
  }
  for (let i = 0; i < form.ingredients.length; i++) {
    if (!form.ingredients[i].name?.trim()) {
      ElMessage.warning(`第 ${i + 1} 个食材的名称不能为空`)
      return false
    }
  }
  return true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    ElMessage.warning('请先补全必填项')
    return
  }
  if (!validateChildren()) {
    return
  }
  submitting.value = true
  try {
    const payload = buildPayload()
    if (form.id) {
      await updateRecipe(payload)
      ElMessage.success('修改成功')
    } else {
      await createRecipe(payload)
      ElMessage.success('新增成功')
    }
    goBack()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = await fetchCategoryList()
  } catch (e) {
    categories.value = []
  }

  const id = route.params.id
  if (!id) {
    return
  }
  loading.value = true
  try {
    const detail = await fetchRecipeDetail(id)
    Object.assign(form, {
      id: detail.id,
      categoryId: detail.categoryId,
      name: detail.name,
      subtitle: detail.subtitle,
      coverUrl: detail.coverUrl,
      description: detail.description,
      cuisine: detail.cuisine,
      taste: detail.taste,
      cookMethod: detail.cookMethod,
      difficulty: detail.difficulty ?? 1,
      cookTime: detail.cookTime ?? 0,
      servings: detail.servings ?? 1,
      calories: detail.calories ?? 0,
      price: detail.price ?? 0,
      originalPrice: detail.originalPrice ?? 0,
      stock: detail.stock ?? 0,
      isRecommend: detail.isRecommend ?? 0,
      status: detail.status ?? 1,
      steps: (detail.steps || []).map((s) => ({
        title: s.title,
        content: s.content,
        imageUrl: s.imageUrl,
        duration: s.duration ?? 0,
        tip: s.tip
      })),
      ingredients: (detail.ingredients || []).map((i) => ({
        name: i.name,
        amount: i.amount,
        unit: i.unit,
        isMain: i.isMain ?? 0,
        note: i.note
      })),
      seasonings: (detail.seasonings || []).map((s) => ({
        name: s.name,
        amount: s.amount,
        unit: s.unit,
        note: s.note
      })),
      tips: (detail.tips || []).map((t) => ({ title: t.title, content: t.content }))
    })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.divider-btn {
  margin-left: 12px;
}

.sub-row {
  padding: 12px;
  margin-bottom: 10px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafcff;
}

.sub-row.inline {
  display: flex;
  gap: 8px;
  align-items: center;
}

.sub-row.inline :deep(.el-input) {
  flex: 1;
}

.sub-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.sub-title {
  width: 240px;
}

.unit {
  font-size: 12px;
  color: #909399;
}

.sub-extra {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.sub-extra :deep(.el-input) {
  flex: 1;
}

.footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>
