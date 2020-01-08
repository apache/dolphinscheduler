<template>
  <h1> {{ msg || 'default message' }}</h1>
</template>

<script>
export default {
  name: 'message',
  props: [
    'msg'
  ]
}
</script>