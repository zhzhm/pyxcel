const CompressionPlugin = require('compression-webpack-plugin')
module.exports = {
  publicPath: '/',
  outputDir: '../src/main/resources/META-INF/resources/',
  devServer: {
    host: '0.0.0.0',
    port: 8110,
    public: 'vmubuntu:8110',
    proxy: {
      '/rest': {
        target: 'http://localhost:8080/'
      },
      '/console': {
        target: 'ws://localhost:8080/',
        ws: true,
        secure: false,
        logLevel: 'debug'
      }
    }
  }
}
