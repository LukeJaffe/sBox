# sBox #

Mac Setup Instructions:
1. open console
2. type:
    - brew update
    - brew install git
    - brew install ant
3. to install project in a directory type:
    - git clone /home/jaffe5/Projects/capstone/code/sBox
4. enter the project directory: install_location/sBox
5. enter the test_app directory: install_location/sBox/test_app 
6. to build the app type:
    - ant clean debug
7. to install the app on a running emulator or physical device type:
    - ant clean debug install

Debug Instructions (if something goes wrong):
for step 6:
    - the output should say BUILD SUCCESSFUL at the end
    - if it doesnt, type:
        - ant clean debug > build_fail.out
        - then send me the build_fail.out file
for step 7:
    - the output should say BUILD SUCCESSFUL at the end
    - if it doesnt, type:
        - ant clean debug install > install_fail.out
        - then send me the install_fail.out file
