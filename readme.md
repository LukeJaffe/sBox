# sBox #

Mac Setup Instructions:
- install Homebrew (you will have to figure this out)
- open console
- type:
    - brew update
    - brew install android-sdk
    - brew install ant
    - brew install git
- to install project in a directory type:
    - git clone /home/jaffe5/Projects/capstone/code/sBox
- enter the project directory: install_location/sBox
- enter the test_app directory: install_location/sBox/test_app 
- to build the app type:
    - ant clean debug
- to install the app on a running emulator or physical device type:
    - ant clean debug install

Debug Instructions (if something goes wrong):
- for step 6:
    - the output should say BUILD SUCCESSFUL at the end
    - if it doesnt, type:
        - ant clean debug > build_fail.out
        - then send me the build_fail.out file
- for step 7:
    - the output should say BUILD SUCCESSFUL at the end
    - if it doesnt, type:
        - ant clean debug install > install_fail.out
        - then send me the install_fail.out file
