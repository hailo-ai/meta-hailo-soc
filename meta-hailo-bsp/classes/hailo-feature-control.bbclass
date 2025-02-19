# Helper function to get package list for enabled features

def get_features_to_enable(d, features_subset):
    """
    Determines the list of enabled features for a a given features subset.
    By default, enable all features in the subset.
    If DISTRO_FEATURES is required, enable features from the subset if they're set in DISTRO_FEATURES.

    Args:
        d: The data store object.
        features_subset: A string, separated by spaces, representing a subset of features to enable.

    Returns:
        A set of features to enable from a given subset of features.
    """
    enabledFeatures = set(features_subset.split())
    if d.getVar('ENABLE_DISTRO_FEATURES') == "True":
        enabledFeatures = enabledFeatures.intersection(set(d.getVar('DISTRO_FEATURES').split()))

    return enabledFeatures

def get_packagegroups_to_install(d, packagegroup_features, packagegroup_name):
    """
    Determines the list of packagegroups to install based on its features and name.
    By default, enable all available feature for a packagegroup (given by packagegroup_features).
    If DISTRO_FEATURES is required, enable features from the packagegroup if they're set in DISTRO_FEATURES.

    Args:
        d: The data store object.
        packagegroup_features: A string, separated by spaces, with all possible features of a packagegroup.
        packagegroup_name: The packagegroup name to which each enabled feature should be added.

    Returns:
        A space-separated string of packagegroup names to be installed.
    """
    features = get_features_to_enable(d, packagegroup_features)
    return " ".join(f"{packagegroup_name}-{feature}" for feature in features)
