java -jar ../../../external/libs/compiler.jar --js jquery.js blockui.js  bootstrap.min.js jquery.Jcrop.min.js kikbak.js --js_output_file kikbak.min.js; scp -r -i ~/Downloads/kikbak.pem ../*.html ../css ../js ubuntu@test.kikbak.me:~/chen/m/
