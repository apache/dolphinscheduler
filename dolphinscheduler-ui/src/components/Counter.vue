<template>
  <div>
    <div>{{ computedCount }}</div>
    <button @click="inc">加</button>
    <button @click="dec">减</button>
    <button @click="reset">重置</button>
  </div>
</template>

<script>
export default {
  props: {
    factor: { type: Number, default: 1 }
  },
  data() {
    return {
      count: 0
    };
  },
  methods: {
    inc() {
      this.count++;
    },
    dec() {
      this.count--;
    },
    reset() {
      this.count = 0;
    }
  },
  computed: {
    computedCount: function() {
      return this.count * this.factor;
    }
  }
};
</script>