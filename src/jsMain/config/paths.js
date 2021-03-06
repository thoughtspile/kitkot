'use strict';

const path = require('path');
const fs = require('fs');
const url = require('url');

// Make sure any symlinks in the project folder are resolved:
// https://github.com/facebookincubator/create-react-app/issues/637
const appDirectory = fs.realpathSync(process.cwd());
const resolveApp = relativePath => path.resolve(appDirectory, relativePath);

const envPublicUrl = process.env.PUBLIC_URL;

function ensureSlash(path, needsSlash) {
  const hasSlash = path.endsWith('/');
  if (hasSlash && !needsSlash) {
    return path.substr(path, path.length - 1);
  } else if (!hasSlash && needsSlash) {
    return `${path}/`;
  } else {
    return path;
  }
}

function hasIdeaDir(dir) {
   try {
       fs.accessSync(path.join(dir, '.idea'));
   } catch {
       return false;
   }
   return true;
}
function getProjectPath(dir) {
    while (dir) {
        if (hasIdeaDir(dir)) {
            return dir;
        }
        dir = path.resolve(dir, '..');
    }
    throw new Error('Cannot locate IDEA project');
}

const findIml = function(directory) {
  const imlList = fs.readdirSync(directory).filter((element) => {
    var extName = path.extname(element);
    return extName === '.iml';
  });
  return imlList.length > 0 && imlList[0] || null
}

const getPublicUrl = appPackageJson =>
  envPublicUrl || require(appPackageJson).homepage;

// We use `PUBLIC_URL` environment variable or "homepage" field to infer
// "public path" at which the app is served.
// Webpack needs to know it to put the right <script> hrefs into HTML even in
// single-page apps that may serve index.html for nested URLs like /todos/42.
// We can't use a relative path in HTML because we don't want to load something
// like /todos/42/static/js/bundle.7289d.js. We have to know the root.
function getServedPath(appPackageJson) {
  const publicUrl = getPublicUrl(appPackageJson);
  const servedUrl =
    envPublicUrl || (publicUrl ? url.parse(publicUrl).pathname : '/');
  return ensureSlash(servedUrl, true);
}

// config after eject: we're in ./config/
module.exports = {
  dotenv: resolveApp('.env'),
  appBuild: resolveApp('build'),
  appPublic: resolveApp('public'),
  appHtml: resolveApp('public/index.html'),
  appPackageJson: resolveApp('package.json'),
  appSrc: resolveApp('kotlin/sample'),
  yarnLockFile: resolveApp('yarn.lock'),
  appNodeModules: resolveApp('node_modules'),
  publicUrl: getPublicUrl(resolveApp('package.json')),
  servedPath: getServedPath(resolveApp('package.json')),
  kotlinOutputPath: resolveApp('node_modules/.cache/kotlin-webpack'),
  projectPath: getProjectPath(resolveApp('.')),
  imlPath: findIml(resolveApp('.')),
  commonSrc: resolveApp('../commonMain/kotlin/sample'),
};
