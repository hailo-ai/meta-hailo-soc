IMAGE_INSTALL:append = " packagegroup-hailo-media-library liberation-fonts"
PACKAGECONFIG:remove:pn-opencv = "gtk gapi dnn eigen gphoto2 java opencl python2 python3 samples tests "