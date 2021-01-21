<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <p>
      Based on Vue and Quarkus framework,<br>
      check out the source code from <a target="_blank" rel="noopener" href="https://github.com/zhzhm/pyxcel">GitHub</a>.
    </p>
    <el-button type="primary" @click="createNew">Create New</el-button>
  </div>
</template>

<script>
import { shortRadomStr } from '../api'
export default {
  name: 'HelloWorld',
  props: {
    msg: String
  },
  methods: {
    createNew () {
      location.href = '/session/' + shortRadomStr()
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
