ARG PYTHON310_IMG
ARG BASE_CONTAINER

FROM $PYTHON310_IMG AS PYTHON310
FROM $BASE_CONTAINER

LABEL Maintainer="Saagie"

################ Kernels / Conda envs / requirements PART BEGIN ################
USER $NB_USER

# Update conda to latest version and install jupyterlab-git plugin
RUN conda update -n root conda \
    && conda install -c conda-forge jupyterlab==3.2.9 jupyterlab-git \
    && conda clean -ay

RUN conda create -n py38 python=3.8.12 \
    && bash -c "source activate py38 && conda install notebook ipykernel -y && ipython kernel install --user --name py38  --display-name 'Python 3.8'" \
    && conda create -n py39 python=3.9.10 \
    && bash -c "source activate py39 && conda install notebook ipykernel -y && ipython kernel install --user --name py39  --display-name 'Python 3.9'" \
    && conda create -n py310 python=3.10.2 \
    && bash -c "source activate py310 && conda install notebook ipykernel -y && ipython kernel install --user --name py310  --display-name 'Python 3.10'" \
    && conda clean -ay \
    && rm -rf ~/.cache/pip

# Updating kernels to force activating conda env whenever opening a console
ADD resources/kernel-3.8.json /home/$NB_USER/.local/share/jupyter/kernels/py38/kernel.json
ADD resources/kernel-3.9.json /home/$NB_USER/.local/share/jupyter/kernels/py39/kernel.json
ADD resources/kernel-3.10.json /home/$NB_USER/.local/share/jupyter/kernels/py310/kernel.json
ADD resources/kernel-*.sh /

# Uninstall default python3 kernel
RUN jupyter kernelspec remove -f python3
SHELL ["/bin/bash", "-c"]
COPY --from=PYTHON310 /tmp/requirements.txt /home/$NB_USER/requirements.txt

RUN sh -x \
    && for env in py38 py39 py310;  \
       do \
          . activate $env \
          && python -m pip install --no-cache-dir -r /home/$NB_USER/requirements.txt \
          && conda deactivate; \
       done \
    && conda clean -ay \
    && rm -rf ~/.cache/pip

################ Kernels / Conda envs / requirements PART ENDS #################

# Should run as $NB_USER  ... but ...
# USER $NB_USER
# Saagie mounts the /notebook-dir as root so it needs to be chown in the entrypoint as root
USER root

# Runs Jupyter in a loop so that quitting Jupyter does not cause the container to exit.
ENV RESTARTABLE yes



