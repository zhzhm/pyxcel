<template>
  <el-container>
    <el-header height="30px">
      <el-row>
        <el-col :span="16">
          <span class="brand">PyXcel</span>
          <div class="spaces" />
          <el-button size="mini" round @click="newSession"> New</el-button>
          <el-button size="mini" round @click="executePython"> Run</el-button>
        </el-col>
        <el-col :span="8" class="right-btns">
          <el-button size="mini" round disabled> Login</el-button>
          <div class="version">{{ version }}</div>
        </el-col>
      </el-row>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-tree
          :data="treeData"
          :props="defaultProps"
          lazy
          :load="loadNode"
          node-key="absPath"
          ref="fileTree"
        >
          <span class="file-list" slot-scope="{ node }">
            <i v-if="!node.data.isFile" class="el-icon-folder"></i>
            <i v-else class="el-icon-document"></i>
            <span>{{ node.label }}</span>
            <el-button
              type="text"
              size="mini"
              @click.stop="() => deleteFile(node)"
              icon="el-icon-delete"
            >
            </el-button>
          </span>
        </el-tree>
        <el-upload
          class="upload-demo"
          action="/rest/file/upload"
          :data="uploadingFileName"
          :headers="uploadingHeader"
          :before-upload="beforeUpload"
          :on-success="uploadSuccess"
          multiple
          :limit="3"
          ref="fileUploader"
        >
          <el-button size="small" type="primary">点击上传</el-button>
          <div slot="tip" class="el-upload__tip">
            建议不超过5MB
          </div>
        </el-upload>
      </el-aside>
      <el-container>
        <el-main>
          <div id="mainPanel" class="editor">
            <codemirror
              ref="pythoncode"
              :value="pythonCode"
              :options="pythonOptions"
              :style="codeStyle"
              class="code"
              @input="onPythonCodeChange"
            />
          </div>
        </el-main>
        <el-footer height="210px">
          <div class="output-title">Output</div>
          <div id="console-log">{{ consoleLog }}</div>
        </el-footer>
      </el-container>
    </el-container>
  </el-container>
</template>

<script>
import { codemirror } from 'vue-codemirror'
import {
  listFiles,
  deleteFile,
  getSid,
  executePython,
  sleep,
  shortRadomStr
} from '../api/index'

import 'codemirror/theme/eclipse.css'
import 'codemirror/mode/python/python'
import 'codemirror/mode/xml/xml'
import 'codemirror/addon/fold/foldcode'
import 'codemirror/addon/fold/foldgutter.css'
import 'codemirror/addon/fold/foldgutter'
import 'codemirror/addon/fold/brace-fold'
import 'codemirror/addon/fold/comment-fold'
export default {
  components: {
    codemirror
  },
  data () {
    return {
      version: '1.0.1',
      mainPanelHeight: window.innerHeight - 30,
      pythonCode: 'print("Hello, World!")',
      pythonOptions: {
        value: '',
        mode: 'text/x-python',
        theme: 'eclipse',
        readOnly: false,
        lineNumbers: true,
        lineWrapping: true,
        foldGutter: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
        matchBrackets: true,
        styleActiveLine: true
      },
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'isFile'
      },
      treeData: [],
      uploadingFileName: {},
      consoleLog: '',
      processCounter: 0,
      sessionCounter: 1,
      wsSessionId: undefined
    }
  },
  created () {
    this.$options.sockets.onmessage = (data) => this._socketMethods(data.data)
  },
  mounted () {
    this.$connect()
    this.consoleLog += 'Ready to run!\n'
  },
  computed: {
    codeStyle () {
      return {
        height: this.mainPanelHeight - 210 + 'px'
      }
    },
    uploadingHeader () {
      return {
        sid: getSid()
      }
    }
  },
  methods: {
    onPythonCodeChange (newValue) {
      this.pythonCode = newValue
    },
    loadNode (node, resolve) {
      listFiles(node.data.absPath)
        .then((resp) => {
          const sortedFiles = resp.data.sort((d1, d2) => {
            if (d1.isFile && !d2.isFile) return 1
            else if (!d1.isFile && d2.isFile) return -1
            else {
              return d1.name.localeCompare(d2.name)
            }
          })
          resolve(sortedFiles)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    beforeUpload (file) {
      this.uploadingFileName.name = file.name
      return true
    },
    uploadSuccess (resp, file) {
      if (
        this.$refs.fileTree.root.childNodes.find(
          (n) => n.data.name === file.name
        )
      ) {
        return
      }
      this.treeData.push({
        name: file.name,
        isFile: true,
        absPath: file.name
      })
      setTimeout(() => this.$refs.fileUploader.clearFiles(), 2000)
    },
    deleteFile (node) {
      this.$confirm('删除文件' + node.label + '? 将无法恢复').then(() =>
        deleteFile(node.data.absPath).then(() => {
          this.$refs.fileTree.remove(node)
        })
      )
    },
    async executePython () {
      await executePython(this.wsSessionId, this.pythonCode).then((resp) =>
        console.log(resp.data)
      )
      this.treeData = await listFiles().then((resp) => resp.data)
    },
    async _checkWebSocket () {
      if (!this.sessionId || !this.$socket || this.$socket.readyState !== 1) {
        this.$connect()
        let counter = 0
        while (counter++ < 10 && !this.sessionId) {
          await sleep(200)
        }
        if (!this.sessionId) {
          this.$alert(
            'Cannot connect to server,\n Please check you connection.',
            'Failure',
            { type: 'error' }
          )
          return false
        }
      }
      return true
    },
    newSession () {
      const radomStr = shortRadomStr()
      const newHref = location.href.substring(0, location.href.lastIndexOf('/') + 1) + radomStr
      location.href = newHref
    },
    _socketMethods (data) {
      if (data.charAt(0) !== '{' || data.charAt(data.length - 1) !== '}') {
        this._toConsole(data)
        return
      }
      try {
        const commandObj = JSON.parse(data)
        switch (commandObj.type) {
          case 'init':
            this._initSession(commandObj.wsId)
            break
          case 'processCounter':
            this.processCounter = commandObj.message
            break
          case 'sessionCounter':
            this.sessionCounter = commandObj.message
            break
          case 'keepAlive':
            console.log(commandObj.message)
            break
          default:
            this._toConsole(data)
        }
      } catch (err) {
        this._toConsole(data)
      }
    },
    _initSession (sid) {
      this.wsSessionId = sid
    },
    _toConsole (log) {
      this.consoleLog += log
      this.$nextTick(() => {
        const logWindow = document.getElementById('console-log')
        logWindow.scrollTop = logWindow.scrollHeight
      })
    }
  }
}
</script>

<style lang="scss">
.el-header,
.el-footer {
  background-color: #606266;
  color: #333;
  text-align: left;
  line-height: 3px;
  height: 32px;
}
.right-btns {
  text-align: right;
}
.right-btns .el-button {
  margin-top: 1px;
}
.el-footer .foot-status {
  position: absolute;
  color: whitesmoke;
  font-size: 14px;
  bottom: 14px;
  right: 5px;
}
.logo {
  display: inline-block;
  color: white;
  font-size: 1.3em;
  vertical-align: middle;
  background-size: 50px;
  width: 60px;
  height: 30px;
}
.brand {
  vertical-align: middle;
  background-size: 50px;
  height: 30px;
  margin-right: 20px;
  background-image: -webkit-linear-gradient(bottom, #78a91c, #b7e85e);
  background-clip: unset;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: Impact, Haettenschweiler, "Arial Narrow Bold", sans-serif;
  font-size: 1.6em;
}
.current-user {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  font-size: 15px;
  margin-left: 15px;
  color: whitesmoke;
}
.el-main {
  background-color: #e9eef3;
  color: #333;
  text-align: left;
  padding: 0 5px !important;
}
.editor .el-col {
  height: 100%;
}
.editor .CodeMirror {
  height: 100%;
}
.viewer {
  background-color: white;
  padding: 5px 0;
}
.viewer .el-col {
  height: 100%;
}
.viewer iframe {
  width: 100%;
  height: 100%;
  border: 0;
}
.viewer iframe.blockPointerEvents {
  pointer-events: none;
}
.CodeMirror-hscrollbar,
.CodeMirror-vscrollbar {
  outline: none;
  border: none;
}
.CodeMirror-hscrollbar::-webkit-scrollbar {
  width: 100%;
  height: 5px;
}
.CodeMirror-vscrollbar::-webkit-scrollbar {
  width: 5px;
  height: 100%;
}
.CodeMirror-hscrollbar::-webkit-scrollbar-thumb,
.CodeMirror-vscrollbar::-webkit-scrollbar-thumb {
  border-radius: 5px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
  background: rgba(0, 0, 0, 0.1);
}

div.spaces {
  display: inline-block;
  width: 2em;
}
.properties-table th,
.properties-table td {
  padding: 2px 2px !important;
}
.el-footer .el-button {
  vertical-align: top;
}
.console-window {
  z-index: 100;
  background: white;
  text-align: left;
}
#console-log {
  padding: 5px 5px;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 18px;
  font-family: "Courier New", Courier, monospace;
  font-size: 14px;
  overflow-y: auto;
  height: 170px;
  color: white;
}
#console-log::-webkit-scrollbar {
  width: 25px;
}
#console-log::-webkit-scrollbar-track {
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}
#console-log::-webkit-scrollbar-thumb {
  border-radius: 12px;
  border: 8px solid rgba(0, 0, 0, 0);
  box-shadow: 12px 0 0 rgb(113, 119, 126) inset;
  min-height: 40px;
}
#console-log .clear-btn {
  position: absolute;
  top: 6px;
  right: 40px;
  background-color: #3e3e3e;
  border: none;
  padding: 4px;
}

#console-log .clear-btn:hover,
.resource-manager .close-btn:hover {
  background-color: rgb(97, 97, 97);
}
button.float-reload {
  position: fixed;
  /* top: 347px; */
  right: 55px;
  z-index: 10;
}
button.float-opennew {
  position: fixed;
  /* top: 347px; */
  right: 185px;
  z-index: 10;
}
.properties-panel {
  overflow-y: auto;
  padding-left: 4px;
}
.properties-form {
  width: 99%;
}
.properties-panel::-webkit-scrollbar {
  width: 5px;
  height: 100%;
}
.properties-panel::-webkit-scrollbar-thumb {
  border-radius: 5px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
  background: rgba(0, 0, 0, 0.1);
}
.properties-form .el-input.vmargs {
  width: auto;
  position: absolute;
  right: 30px;
  left: 0;
  line-height: 30px;
}
.properties-form button.remove {
  position: absolute;
  top: 2px;
  right: 0;
  padding: 4px 4px 3px 4px;
  font-size: 16px;
}
.properties-form button.add {
  position: absolute;
  right: 0px;
  top: 2px;
  padding: 4px 4px 3px 4px;
  font-size: 12px;
  z-index: 100;
}
.properties-form button.add i {
  font-size: 16px;
}
.properties-form button.add span {
  vertical-align: text-top;
}
.properties-form .el-form-item {
  margin-bottom: 0;
}
.properties-form .el-form-item .el-form-item__error {
  position: absolute;
  top: 4px;
  right: 10px;
  left: auto;
  width: 50px;
  text-align: right;
  font-size: 10px;
}
.properties-form .el-form-item .el-select {
  width: 100%;
}
.properties-form .el-form-item .el-form-item__content,
.properties-form .el-form-item .el-input__inner,
.properties-form .el-form-item label {
  height: 25px;
  line-height: 25px;
  font-size: 12px;
}
.additional-deps {
  margin: -20px 0 0 5px;
  position: relative;
}
.additional-deps > div {
  text-align: left;
  padding-top: 7px;
  position: relative;
  height: 17px;
  line-height: 12px;
  font-size: 12px;
}
.additional-deps .item:hover {
  background-color: rgb(216, 215, 228);
}
.additional-deps .item {
  padding-right: 28px;
}
.additional-deps .header {
  display: inline-block;
  font-weight: bolder;
  text-align: left;
  width: 150px;
  border-top: solid 1px gray;
}
.additional-deps .spaces {
  position: absolute;
  height: 17px;
  top: 0;
  width: 7px;
  background: linear-gradient(
    75deg,
    transparent 45%,
    gray 45%,
    gray 52%,
    transparent 52%
  );
}
.additional-deps .bottom-line {
  border-bottom: solid 1px gray;
  position: absolute;
  width: auto;
  height: 16px;
  left: 157px;
  right: 0;
  top: 0px;
}
.resize-bar-vertical {
  z-index: 100;
  width: 99%;
  height: 6px;
  cursor: row-resize;
  background-color: rgba(0, 0, 0, 0.07);
  position: absolute;
}
.resize-bar-vertical:hover {
  background-color: rgba(0, 0, 0, 0.2);
}
.resize-bar-horizontal {
  position: relative;
  z-index: 100;
  width: 6px;
  margin-right: -6px;
  height: 98%;
  cursor: col-resize;
  background-color: rgba(0, 0, 0, 0.05);
  float: left;
}
.resize-bar-horizontal:hover {
  background-color: rgba(0, 0, 0, 0.2);
}
.file-list span {
  margin: 0 5px;
}
.version {
  display: inline-block;
  color: lightgray;
  margin: 3px 5px;
}
.output-title {
  height: 1em;
  margin-top: 8px;
  color:white;
}
</style>
