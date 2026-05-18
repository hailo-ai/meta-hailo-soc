inherit hailo-image-base
inherit deploy

IMAGE_INSTALL:append = " packagegroup-hailo-bsp"

# Deploy .verity.env file to our deploy dir
do_deploy() {
    # Deploy verity env file only if securefs is enabled
    if [ "${@bb.utils.contains('DISTRO_FEATURES', 'securefs', 'securefs', '', d)}" = "securefs" ]; then
        install -m 644 ${STAGING_VERITY_DIR}/${IMAGE_BASENAME}.${DM_VERITY_IMAGE_TYPE}.verity.env ${DEPLOYDIR}/
    fi
}
addtask deploy before do_build

do_deploy[depends] += "${@bb.utils.contains('DISTRO_FEATURES', 'securefs', ' %s:do_image_%s' % (d.getVar('HAILO_TARGET'), d.getVar('DM_VERITY_IMAGE_TYPE')), '', d)}"