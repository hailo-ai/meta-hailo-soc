PACKAGECONFIG:append = " freetype"
PACKAGECONFIG:remove = "${@bb.utils.contains('DISTRO_FEATURES', 'hailo-core', 'gtk gapi dnn eigen gphoto2 java opencl python2 python3 samples tests', '', d)}"