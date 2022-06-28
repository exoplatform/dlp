const path = require('path');
const { merge } = require('webpack-merge');

const webpackProductionConfig = require('./webpack.prod.js');

// the display name of the war
const app = 'dlp';

// add the server path to your server location path

const exoServerPath = "/exo-server";


module.exports = merge(webpackProductionConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`)
  },
  mode: 'development',
  devtool: 'inline-source-map'
})
