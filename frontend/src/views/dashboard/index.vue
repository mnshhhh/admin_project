<template>
  <div>
    <AdminDashboard v-if="userStore.hasPermission('report:asset')" />
    <StudentDashboard v-else />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'
import AdminDashboard from './AdminDashboard.vue'
import StudentDashboard from './StudentDashboard.vue'

const userStore = useUserStore()
</script>
