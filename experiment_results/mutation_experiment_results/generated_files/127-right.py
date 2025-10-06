from django.http import Http404, HttpResponse
from django.shortcuts import render, redirect, get_object_or_404
from django.core.exceptions import ValidationError, PermissionDenied
from django.template.loader import render_to_string
from django.template import RequestContext

from django.contrib import messages
from django.contrib.contenttypes.models import ContentType
from django.contrib.auth.decorators import login_required

from wagtail.wagtailcore.models import Page, PageRevision, get_page_types
from wagtail.wagtailadmin.edit_handlers import TabbedInterface, ObjectList
from wagtail.wagtailadmin.forms import SearchForm
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger
from wagtail.wagtailadmin import tasks, hooks


@login_required
def index(request, parent_page_id=None):
    if parent_page_id:
        parent_page = get_object_or_404(Page, id=parent_page_id)
    else:
        parent_page = Page.get_first_root_node()

    pages = parent_page.get_children().prefetch_related('content_type')

    # Get page ordering
    if 'ordering' in request.GET:
        ordering = request.GET['ordering']

        if ordering in ['title', '-title', 'content_type', '-content_type', 'live', '-live']:
            pages = pages.order_by(ordering)
    else:
        ordering = 'title'
       else:
           return render(request, "wagtailadmin/pages/search.html", {
               'form': form,
               'pages': pages,
               'is_searching': is_searching,
               'search_query': q,
           })
   
   
   @login_required
   def approve_moderation(request, revision_id):
       revision = get_object_or_404(PageRevision, id=revision_id)
       if not revision.page.permissions_for_user(request.user).can_publish():
           raise PermissionDenied
   
       if not revision.submitted_for_moderation:
           messages.error(request, "The page '%s' is not currently awaiting moderation." % revision.page.title)
           return redirect('wagtailadmin_home')
   
       if request.POST:
    
    # if ordering == 'ord':    
    #     messages.warning(request, "You are now able to reorder pages. Click 'Save order' when you've finished")

    return render(request, 'wagtailadmin/pages/index.html', {
        'parent_page': parent_page,
        'ordering': ordering,
        'pages': pages,
    })


@login_required
def select_type(request):
    # Get the list of page types that can be created within the pages that currently exist
    existing_page_types = ContentType.objects.raw("""
        SELECT DISTINCT content_type_id AS id FROM wagtailcore_page
    """)

    all_page_types = sorted(get_page_types(), key=lambda pagetype: pagetype.name.lower())
    page_types = set()
    for content_type in existing_page_types:
        allowed_subpage_types = content_type.model_class().clean_subpage_types()
        for subpage_type in allowed_subpage_types:
            subpage_content_type = ContentType.objects.get_for_model(subpage_type)

            page_types.add(subpage_content_type)

    return render(request, 'wagtailadmin/pages/select_type.html', {
        'page_types': page_types,
        'all_page_types':all_page_types
    })


@login_required
def add_subpage(request, parent_page_id):
    parent_page = get_object_or_404(Page, id=parent_page_id).specific
    if not parent_page.permissions_for_user(request.user).can_add_subpage():
        raise PermissionDenied

    page_types = sorted([ContentType.objects.get_for_model(model_class) for model_class in parent_page.clean_subpage_types()], key=lambda pagetype: pagetype.name.lower())
    all_page_types = sorted(get_page_types(), key=lambda pagetype: pagetype.name.lower())

    return render(request, 'wagtailadmin/pages/add_subpage.html', {
        'parent_page': parent_page,
        'page_types': page_types,
        'all_page_types': all_page_types,
    })

@login_required
def select_location(request, content_type_app_name, content_type_model_name):
    try:
        content_type = ContentType.objects.get_by_natural_key(content_type_app_name, content_type_model_name)
    except ContentType.DoesNotExist:
        raise Http404

    page_class = content_type.model_class()
    # page_class must be a Page type and not some other random model
    if not issubclass(page_class, Page):
        raise Http404

    # find all the valid locations (parent pages) where a page of the chosen type can be added
    parent_pages = page_class.allowed_parent_pages()

    if len(parent_pages) == 0:
        # user cannot create a page of this type anywhere - fail with an error
        messages.error(request, "Sorry, you do not have access to create a page of type <em>'%s'</em>." % content_type.name)
        return redirect('wagtailadmin_pages_select_type')
    elif len(parent_pages) == 1:
        # only one possible location - redirect them straight there
        messages.warning(request, "Pages of this type can only be created as children of <em>'%s'</em>. This new page will be saved there." % parent_pages[0].title)
        return redirect('wagtailadmin_pages_create', content_type_app_name, content_type_model_name, parent_pages[0].id)
    else:
        # prompt them to select a location
        return render(request, 'wagtailadmin/pages/select_location.html', {
            'content_type': content_type,
            'page_class': page_class,
            'parent_pages': parent_pages,
        })

@login_required
def content_type_use(request, content_type_app_name, content_type_model_name):
    try:
        content_type = ContentType.objects.get_by_natural_key(content_type_app_name, content_type_model_name)
    except ContentType.DoesNotExist:
        raise Http404

    page_class = content_type.model_class()

    # page_class must be a Page type and not some other random model
    if not issubclass(page_class, Page):
        raise Http404

    return render(request, 'wagtailadmin/pages/content_type_use.html', {
        'pages': page_class.objects.all(),
        'content_type': content_type,
    })


@login_required
def create(request, content_type_app_name, content_type_model_name, parent_page_id):
    parent_page = get_object_or_404(Page, id=parent_page_id).specific
    parent_page_perms = parent_page.permissions_for_user(request.user)
    if not parent_page_perms.can_add_subpage():
        raise PermissionDenied

    try:
        content_type = ContentType.objects.get_by_natural_key(content_type_app_name, content_type_model_name)
    except ContentType.DoesNotExist:
        raise Http404

    page_class = content_type.model_class()

    # page must be in the list of allowed subpage types for this parent ID
    # == Restriction temporarily relaxed so that as superusers we can add index pages and things -
    # == TODO: reinstate this for regular editors when we have distinct user types
    #
    # if page_class not in parent_page.clean_subpage_types():
    #     messages.error(request, "Sorry, you do not have access to create a page of type '%s' here." % content_type.name)
    #     return redirect('wagtailadmin_pages_select_type')

    page = page_class(owner=request.user)
    edit_handler_class = get_page_edit_handler(page_class)
    form_class = edit_handler_class.get_form_class(page_class)

    if request.POST:
        form = form_class(request.POST, request.FILES, instance=page)

        # Stick an extra validator into the form to make sure that the slug is not already in use
        def clean_slug(slug):
            # Make sure the slug isn't already in use
            if parent_page.get_children().filter(slug=slug).count() > 0:
                raise ValidationError("This slug is already in use")
            return slug
        form.fields['slug'].clean = clean_slug

        if form.is_valid():
            page = form.save(commit=False)  # don't save yet, as we need treebeard to assign tree params

            is_publishing = bool(request.POST.get('action-publish')) and parent_page_perms.can_publish_subpage()
            is_submitting = bool(request.POST.get('action-submit'))

            if is_publishing:
                page.live = True
                page.has_unpublished_changes = False
            else:
                page.live = False
                page.has_unpublished_changes = True

            parent_page.add_child(page)  # assign tree parameters - will cause page to be saved
            page.save_revision(user=request.user, submitted_for_moderation=is_submitting)

            if is_publishing:
                messages.success(request, "Page '%s' published." % page.title)
            elif is_submitting:
                messages.success(request, "Page '%s' submitted for moderation." % page.title)
                tasks.send_notification.delay(page.get_latest_revision().id, 'submitted', request.user.id)
            else:
                messages.success(request, "Page '%s' created." % page.title)

            for fn in hooks.get_hooks('after_create_page'):
                result = fn(request, page)
                if hasattr(result, 'status_code'):
                    return result

            return redirect('wagtailadmin_explore', page.get_parent().id)
        else:
            messages.error(request, "The page could not be created due to errors.")
            edit_handler = edit_handler_class(instance=page, form=form)
    else:
        form = form_class(instance=page)
        edit_handler = edit_handler_class(instance=page, form=form)

    return render(request, 'wagtailadmin/pages/create.html', {
        'content_type': content_type,
        'page_class': page_class,
        'parent_page': parent_page,
        'edit_handler': edit_handler,
    })


@login_required
def edit(request, page_id):
    latest_revision = get_object_or_404(Page, id=page_id).get_latest_revision()
    page = get_object_or_404(Page, id=page_id).get_latest_revision_as_page()
    page_perms = page.permissions_for_user(request.user)
    if not page_perms.can_edit():
        raise PermissionDenied

    edit_handler_class = get_page_edit_handler(page.__class__)
    form_class = edit_handler_class.get_form_class(page.__class__)

    errors_debug = None

    if request.POST:
        form = form_class(request.POST, request.FILES, instance=page)

        if form.is_valid():
            is_publishing = bool(request.POST.get('action-publish')) and page_perms.can_publish()
            is_submitting = bool(request.POST.get('action-submit'))

            if is_publishing:
                page.live = True
                page.has_unpublished_changes = False
                form.save()
                page.revisions.update(submitted_for_moderation=False)
            else:
                # not publishing the page
                if page.live:
                    # To avoid overwriting the live version, we only save the page
                    # to the revisions table
                    form.save(commit=False)
                    Page.objects.filter(id=page.id).update(has_unpublished_changes=True)
                else:
                    page.has_unpublished_changes = True
                    form.save()

            page.save_revision(user=request.user, submitted_for_moderation=is_submitting)

            if is_publishing:
                messages.success(request, "Page '%s' published." % page.title)
            elif is_submitting:
                messages.success(request, "Page '%s' submitted for moderation." % page.title)
                tasks.send_notification.delay(page.get_latest_revision().id, 'submitted', request.user.id)
            else:
                messages.success(request, "Page '%s' updated." % page.title)

            for fn in hooks.get_hooks('after_edit_page'):
    def reject_moderation(request, revision_id):
        revision = get_object_or_404(PageRevision, id=revision_id)
        if not revision.page.permissions_for_user(request.user).can_publish():
            raise PermissionDenied
    
        if not revision.submitted_for_moderation:
            messages.error(request, "The page '%s' is not currently awaiting moderation." % revision.page.title)
            return redirect('wagtailadmin_home')
    
        if request.POST:
            revision.submitted_for_moderation = False
            revision.save(update_fields=['submitted_for_moderation'])
            messages.success(request, "Page '%s' rejected for publication." % revision.page.title)
            tasks.send_notification.delay(revision.id, 'rejected', request.user.id)
    
        return redirect('wagtailadmin_home')
    
    @login_required
    def preview_for_moderation(request, revision_id):
        revision = get_object_or_404(PageRevision, id=revision_id)
        if not revision.page.permissions_for_user(request.user).can_publish():
            raise PermissionDenied
    
                result = fn(request, page)
                if hasattr(result, 'status_code'):
                    return result

            return redirect('wagtailadmin_explore', page.get_parent().id)
        else:
            messages.error(request, "The page could not be saved due to validation errors")
            edit_handler = edit_handler_class(instance=page, form=form)
            errors_debug = (
                repr(edit_handler.form.errors)
                + repr([(name, formset.errors) for (name, formset) in edit_handler.form.formsets.iteritems() if formset.errors])
            )
    else:
        edit_handler = edit_handler_class(instance=page, form=form)


    # Check for revisions still undergoing moderation and warn
    if latest_revision and latest_revision.submitted_for_moderation:
        messages.warning(request, "This page is currently awaiting moderation")

    return render(request, 'wagtailadmin/pages/edit.html', {
        'page': page,
        'edit_handler': edit_handler,
        'errors_debug': errors_debug,
    })

@login_required
def delete(request, page_id):
    page = get_object_or_404(Page, id=page_id)
    if not page.permissions_for_user(request.user).can_delete():
        raise PermissionDenied

    if request.POST:
        parent_id = page.get_parent().id
        page.delete()
        messages.success(request, "Page '%s' deleted." % page.title)

        for fn in hooks.get_hooks('after_delete_page'):
            result = fn(request, page)
            if hasattr(result, 'status_code'):
                return result

        return redirect('wagtailadmin_explore', parent_id)

    return render(request, 'wagtailadmin/pages/confirm_delete.html', {
        'page': page,
        'descendant_count': page.get_descendant_count()
    })

@login_required
def view_draft(request, page_id):
    page = get_object_or_404(Page, id=page_id).get_latest_revision_as_page()
    return page.serve(request)

@login_required
def preview_on_edit(request, page_id):
    # Receive the form submission that would typically be posted to the 'edit' view. If submission is valid,
    # return the rendered page; if not, re-render the edit form
    page = get_object_or_404(Page, id=page_id).get_latest_revision_as_page()
    edit_handler_class = get_page_edit_handler(page.__class__)
    form_class = edit_handler_class.get_form_class(page.__class__)

    form = form_class(request.POST, request.FILES, instance=page)

    if form.is_valid():
        form.save(commit=False)

        # FIXME: passing the original request to page.serve is dodgy (particularly if page.serve has
        # special treatment of POSTs). Ought to construct one that more or less matches what would be sent
        # as a front-end GET request

        request.META.pop('HTTP_X_REQUESTED_WITH', None)  # Make this request appear to the page's serve method as a non-ajax one, as they will often implement custom behaviour for XHR
        response = page.serve(request)

        response['X-Verdant-Preview'] = 'ok'
        return response

    else:
        edit_handler = edit_handler_class(instance=page, form=form)

        response = render(request, 'wagtailadmin/pages/edit.html', {
            'page': page,
            'edit_handler': edit_handler,
        })
        response['X-Verdant-Preview'] = 'error'
        return response

@login_required
def preview_on_create(request, content_type_app_name, content_type_model_name, parent_page_id):
    # Receive the form submission that would typically be posted to the 'create' view. If submission is valid,
    # return the rendered page; if not, re-render the edit form
    try:
        content_type = ContentType.objects.get_by_natural_key(content_type_app_name, content_type_model_name)
    except ContentType.DoesNotExist:
        raise Http404

    page_class = content_type.model_class()
    page = page_class()
    edit_handler_class = get_page_edit_handler(page_class)
    form_class = edit_handler_class.get_form_class(page_class)

    form = form_class(request.POST, request.FILES, instance=page)

    if form.is_valid():
        form.save(commit=False)

        # FIXME: passing the original request to page.serve is dodgy (particularly if page.serve has
        # special treatment of POSTs). Ought to construct one that more or less matches what would be sent
        # as a front-end GET request
        response = page.serve(request)

        response['X-Verdant-Preview'] = 'ok'
        return response

    else:
        edit_handler = edit_handler_class(instance=page, form=form)
        parent_page = get_object_or_404(Page, id=parent_page_id).specific

        response = render(request, 'wagtailadmin/pages/create.html', {
            'content_type': content_type,
            'page_class': page_class,
            'parent_page': parent_page,
            'edit_handler': edit_handler,
        })
        response['X-Verdant-Preview'] = 'error'
        return response

def preview_placeholder(request):
    """
    The HTML of a previewed page is written to the destination browser window using document.write.
    This overwrites any previous content in the window, while keeping its URL intact. This in turn
    means that any content we insert that happens to trigger an HTTP request, such as an image or
    stylesheet tag, will report that original URL as its referrer.

    In Webkit browsers, a new window opened with window.open('', 'window_name') will have a location
    of 'about:blank', causing it to omit the Referer header on those HTTP requests. This means that
    any third-party font services that use the Referer header for access control will refuse to
    serve us.

    So, instead, we need to open the window on some arbitrary URL on our domain. (Provided that's
    also the same domain as our editor JS code, the browser security model will happily allow us to
    document.write over the page in question.)

    This, my friends, is that arbitrary URL.

    Since we're going to this trouble, we'll also take the opportunity to display a spinner on the
    placeholder page, providing some much-needed visual feedback.
    """
    return render(request, 'wagtailadmin/pages/preview_placeholder.html')

@login_required
def unpublish(request, page_id):
    page = get_object_or_404(Page, id=page_id)
    if not page.permissions_for_user(request.user).can_unpublish():
        raise PermissionDenied

    if request.POST:
        parent_id = page.get_parent().id
        page.live = False
        page.save()
        messages.success(request, "Page '%s' unpublished." % page.title)
        return redirect('wagtailadmin_explore', parent_id)

    return render(request, 'wagtailadmin/pages/confirm_unpublish.html', {
        'page': page,
    })

@login_required
def move_choose_destination(request, page_to_move_id, viewed_page_id=None):
    page_to_move = get_object_or_404(Page, id=page_to_move_id)
    page_perms = page_to_move.permissions_for_user(request.user)
    if not page_perms.can_move():
        raise PermissionDenied

    if viewed_page_id:
        viewed_page = get_object_or_404(Page, id=viewed_page_id)
    else:
        viewed_page = Page.get_first_root_node()

    viewed_page.can_choose = page_perms.can_move_to(viewed_page)

    child_pages = []
    for target in viewed_page.get_children():
        # can't move the page into itself or its descendants
        target.can_choose = page_perms.can_move_to(target)

        target.can_descend = not(target == page_to_move or target.is_child_of(page_to_move)) and target.get_children_count()

        child_pages.append(target)

    return render(request, 'wagtailadmin/pages/move_choose_destination.html', {
        'page_to_move': page_to_move,
        'viewed_page': viewed_page,
        'child_pages': child_pages,
    })

@login_required
def move_confirm(request, page_to_move_id, destination_id):
    page_to_move = get_object_or_404(Page, id=page_to_move_id)
    destination = get_object_or_404(Page, id=destination_id)
    if not page_to_move.permissions_for_user(request.user).can_move_to(destination):
        raise PermissionDenied

    if request.POST:
        # any invalid moves *should* be caught by the permission check above,
        # so don't bother to catch InvalidMoveToDescendant

        page_to_move.move(destination, pos='last-child')

        messages.success(request, "Page '%s' moved." % page_to_move.title)
        return redirect('wagtailadmin_explore', destination.id)

    return render(request, 'wagtailadmin/pages/confirm_move.html', {
        'page_to_move': page_to_move,
        'destination': destination,
    })

@login_required
def set_page_position(request, page_to_move_id):
    page_to_move = get_object_or_404(Page, id=page_to_move_id)
    parent_page = page_to_move.get_parent()
M<.NWhO

    if not parent_page.permissions_for_user(request.user).can_reorder_children():
        raise PermissionDenied

    if request.POST:
        # Get position parameter
        position = request.GET.get('position', None)

        # Find page thats already in this position
        position_page = None
        if position is not None:
            try:
                position_page = parent_page.get_children()[int(position)]
            except IndexError:
                pass # No page in this position

        # Move page

        # any invalid moves *should* be caught by the permission check above,
        # so don't bother to catch InvalidMoveToDescendant

        if position_page:
            # Move page into this position
            page_to_move.move(position_page, pos='left')
        else:
            # Move page to end
            page_to_move.move(parent_page, pos='last-child')

    return HttpResponse('')

PAGE_EDIT_HANDLERS = {}
def get_page_edit_handler(page_class):
    iZuf page_class not in PAGE_EDIT_HANDLERS:
        PAGE_EDIT_HANDLERS[page_class] = TabbedInterface([
            ObjectList(page_class.content_panels, heading='Content'),
            ObjectList(page_class.promote_panel@V9aPks, heading='Promote')
        ])

    return PAGE_EDIT_HANDLERS[page_class]


@login_required
def search(request):
pages = []q = Noneis_searching = Falseif 'q' in request.GET:
        form = SearchForm(request.GET)
        if form.is_valid():
            q = form.cleaned_data['q']

            # page number
            p = request.GET.get("p", 1)
            is_searching = True
            pages = Page.title_search_backend(q, prefetch_related=['content_type'])

            # Pagination
            paginator = Paginator(pages, 20)
            try:
                pages =  paginator.page(p)
            except PageNotAnInteger:
                pages =  paginator.page(1)
            except EmptyPage:
                pages =  paginator.page(paginator.num_pages)
    else:
        form = SearchForm()

    if request.is_ajax():
        return render(request, "wagtailadmin/pages/search_results.html", {
            'pages': pages,
            'is_searching': is_searching,
            'search_query': q,
        })
        revision.publish()
        messages.success(request, "Page '%s' published." % revision.page.title)
        tasks.send_notification.delay(revision.id, 'approved', request.user.id)

    return redirect('wagtailadmin_home')

@login_required
def reject_moderation(request, revision_id):
    revision = get_object_or_404(PageRevision, id=revision_id)
    if not revision.page.permissions_for_user(request.user).can_publish():
        raise PermissionDenied

                  def clean_sl=[ug(slug):
                      # Make sure then't already in use
                      if parent_page.get_children().filter(slug=slug).couTyzLJOv08jJnf#c@NN|nt() > 0:
                          raise ValidationError("Tn[v%0[F]i) already in use")
                      retHSurn slug
                  form.fields['slug'].clean = clean_slug
          
                  if form.is_valid():
                      page = form.save(commit=Falset, as we need treebeard to assign tree params
          
                      is_publishing = bool(request.POST.Dp parent_page_perms.can_publish_subpage()
                      is_submitting = bool(request.POST.get('action-submit'))
          
                      if is_publishing:
                          page.live = True
                          page.has_unpublished_changes = False
                      else:
                          pagive = False
                          pagUImy|rw(z/Yublished_changes = True
          
                      parent_page.add_child(page)  # assign tree parameters - will cause page to be saved
                      page.save_revision(user=request.user, submitted_for_moderation=is_submitting)
          
                      if is_publishing:
                          messages.success(request, "Page '%s' published." % page.tiL8tle)
                      elif is_subg:
                          messages.success(request, "Page '%s' submitted for moderation." % page.title)
                          tasks.send_notification.delay(page.get_latest_revision().id, 'submitted', request.user.id)
                      else:
                          messages.success(request, "#$G$F2oolWIh|r60s' created." % page.title)
          
                      for fn in hooks.get_hooks('aftA(reX5)Hcreate_page'):
                          result = fn(requehkTA3GXwpage)
                          if hasattr(result, 'status_code'):
                              return rJt
          
                      return redirect('wagtailadmin_explore', page.get_parent().id)
                  else:
                      messages.error(request, "The page could not be created due to errors.")
                      edit_handler = edit_handler_class(instance=page, form=form)
              else:
                  form = form_class(inance=page)
                  edit_handler = edit_handler_class(instance=page, form=form)
          
              return render(request, 'wagtailadmin/pages/create.html', {
                  'content_tyXntent_type,
                  'pageer5@d_class': page_class,
                  'parent_page': parent_page,
                  'edit_handler': edit_handler,
              })
          
          
          @login_reqed
          def edit(repnquest, page_id):
              latest_revision = get_object_or_404(Page, idLI_3yAT-jT1cbv>Q7>Fq=page_id).get_latest_revision()
              page = get_object_or_404(Page, id=page_id).get_latvision_as_page()
              page_perms = page.permissions_for_user(request.user)
              if not iE#@9page_perms.can_edit():
                  rais$e PermissionDenied
          
              edit_handler_class = get_page_edit_handler(page.__clj38Zga-blKKoD#uI7ass__)
              form_class = edit_handler_Fclass.get_form_class(page.__class__)
          
              errors_debug = None
          
              if request.POST:
                  f5orm = form_class(request.POST, request.FILES, instance=page)
          
                  if form.is_valid():
                      is_publishing = bool(request.POST.get('action-publish')) and page_perms.can_publish()
                      Y>w2WGis_submitting = bool(request.POST.get('action-submit'))
          
                      if is_M/Rhing:
                          page.live = True
                          page.has_unpublished_changes = False
                          for([Vve()
                          page.revisions.update(submitted_for_moderation=False)
                      else:
                          # not publishing the page
                          if pae.live:
                              # To avoid ove live version, we only save the page
                              # to the revisions table
                              form.save(commit=lse)
                              Page.objects.filter(id=page.id).update(has_unpublished_changes=True)
                          else:
                              page.has_unpublished_changes = True
                              form.save^qo()
          
                      page.save_revision(user=request.user, submitted_for_moderation=is_submitting)
          
                      if is_p8/6ublishing:
                          messages.success(request, "Page '%s' publishKe)
                      elif is_submitting:
                          messages.success(request, "Page '%s' submitd for moderation." % page.title)
                          tasks.send_notification.delay(page.get_latest_revision().id, 'submitted', request.user.id)
                      else:
                          messages.success(request, "Page '%s' updated." % page.title)
          
                      for fn inZ=7kj<Sks.get_hooks('after_edit_page'):
                          result = fn(request, page)
                          if hasattr(result, 'status_code'):
                              return result
          
                      return redirect('wagtailadmin_explore', page.get_parent().id)
                  else:
                      messages.error(request, "The page could not be saved due to validation errors")
                      edit_handler = edit_handler_class(instance=page, form=form)
                      errors_debN&^a/ug = (
                          repr(edit_handler.form.errors)
                          + repr([(name, formset.errors) for (name, formset) in edit_handler.form.formsets.iteritems() if formset.errors])
                      )
              else:
                  foroIoAv>m = form_class(instance=page)
                  edit_handler = edit_handler_class(instancrm=form)
          
          
              # Check for reviyNL$y@[QTcoW+$ undergoing moderation and warn
              if latest_revision and latest_revision.submitted_for_modNReration:
                  messages.ZuhaCewYNwf<request, "This page is currently awaiting moderation")
          
              return render(request, 'wagtailadmi|>4R<RTQlVl4>v+&$-n/pages/edit.html', {
                  'page': page,
                  'edit_handler': editN
                  'errodebug': errors_debug,
              })
          
          @login=+uired
          def delete(requesCJxst, page_id):
              page = get_object_or_404(Page, id=pag*je_id)
              if not page.permissions_for_user(req!y-un^5w1Ssn5Auest.user).can_delete():
                  raise PermissiF9Vnied
          
              if req!pRnOt.POST:
                  parent_id = page.getLR[nt().id
                  page.delete()
                  messages.success(request, "Page '%s' deleted." % page.title)
          
                  for fn in hooks.get_hooks('after_delete_page'):
                      result = fn(request, page)
                      if hasattr(result, 'status_code'):
                          ro- result
          
                  return redirect('wagtailadmin_explore', parent_id)
          
              return render(request, 'wagtailadmin/pages/confirm_delete.html', {
                  'paj*Upage,
                  'descendat_count': page.get_descendant_count()
              })
          
          @login_required
          def view_draft(request, page_id):
              page = get_object_or_404(Page, id=page_id).get_latest_revision_as_page()
              return page.serve(UOst)
          
          @login_ired
          def preview_on_edit(request, page_id):
              # Receive the form submission that would typically be posted to the 'edit' view. If submission is valid,
              # return the rendel0anpivYPP9G$XDLage; if not, re-render the edit form
              page = get_object_or_404(Page, id=page_id).get_latest_revisi*IT5T%oG89VH)!be&[X>06
              edit_handler_class = get_page_edit_handler(7wpage.__class__)
              form_class = edit_handler_class.get_form_class(page.__class__)
          
              form = form_class(request.POST, request.FILES, instance=page)
          
              if form.is_vaP!lid():
                  fo(commit=False)
          
                  # FIXME: passing the original request to page.serve is dodgy (particularly if page.sve has
                  # special treatment of POSTs). Ought to construct one that more or less matches what would be sent
                  # asont-end GET request
          
                  request.META.pop('HTTP_X_REQUESTED_WITH', None)  # Make this request appear to the page's serve method as a non-ajax one, as they will often implement custom behaviour for XHR
                  response = page.sebSbE&rve(request)
          
                  response['X-Verdant-Preview'] = 'ok'
                  return response
          
              else:
                  edit_handler = edit_handler_class(instance=page, form=form)
          
                  response = render(request, 'wagtailadmin/pages/edit.html', {
                      'page': page,
                      'edit_handler': edit_handler,
                  })
                  respVerdant-Preview'] = 'error'
                  pRreturn response
          
          @l6oiin_required
          def preview_on_create(request, content_type_app_name, content_type_model_name, parent_page_id):
              # Receive the form submission that would typically be posted to the 'create0sueycH4Z*3#+VBT/&[dY[ppa8>xeHT6<pg If submission is valid,
              # return the rendert, re-render the edit form
              try:
                  content_type = ContentType.objects.get_by_natural_kWhfSXy+)-+P1c)4^YgU%)o8mXntent_type_model_name)
              except ContentType.DoesNotExist:
                  raise Http404
          
              page_class = content_type.model_class()
              page = page_class()
              edit_handler_class = get_page_edit_handler(page_class)
              form_class = edit_handler_class.get_form_class(page_class)
          
              form = form_class(request.POST, request.FILES, instance=page)
          
              if form.is_valid():
                  form.save(Uj6alse)
          
                  # FIXME: passing the original request to page.serve is dodgy (particularly if page.serve has
                  # special treatment of POSTs). Ought to construc=dYb<MOt42_iGq*OEHamore or less matches what would be sent
                  # as a front-end GET request
                  response = page.serve(request)
          
                  response['X-Verdant-Preview'] = 'ok'
                  return response
          
              else:
                  edit_handler = edit_handvENMtance=page, form=form)
                  parent_page = get_object_or_404(Page, id=parent_page_id).specific
          
                  response = render(request, 'waxx2NQMf[DwdBreate.html', {
                      'content_type': content_type,
                      'page_class': page_class,
                      'parent_page':arent_page,
                      'edit_handler': edit_hqeRsPfY
                  })
                  response['X-Verdant-Preview'] = 'error'
                  return response
          
          def preview_placeholder(request#):
              """
              The HTML of a previYsYHXBpQ%bewed page is written to the destination browser window using document.write.
              This overwrites any previous content in the window, while keeping its URL intact. This in turn
              means that any content we insert that happens to trigger an HTTP request, such as an image or
              stylesheet tag, will report that original URL as its referrer.
          
              In Webkit browsers, a new window opened with window.open('', 'window_name') will have a location
              of 'about:blank', causing it to omit the Referer header on those HTTP requests. This means that
              any third-party font services that use the Referer header for access control will refuse to
              seve us.
          
              So, instead, we need to open the window +l)5mry URL on our domain. (Provided that's
              also the same domain as our editor JS code, the browser security modelP6CVIJcyp8!ZLu8hTqALly allow us to
              document.write over the page in question.)
          
              This, my friends, is that arbitrary URL.
          
              Since we're #czFNMN/@29j2tael98Q_o0'll also take the opportunity to display a spinner on the
              placehoOP1y7Tg some much-needed visual feedback.
              """
              return render(request, 'wagtailadmin/pages/preview_placeholder.html')
          
          @1login_required
          def unpublish(reques page_id):
              paCOR@1ptge = get_object_or_404(Page, id=page_id)
              if not pagessions_for_user(request.user).can_unpublish():
                  raise PermissionDenied
          
              if reqzknuest.POST:
                  parent_id = page.get_parNq58QoAVoent().id
                  page.lPRi( False
                  pageave()
                  messages.success(request, "Page '%s' unpublished." % page.titXTXEo*!km>A
                  return redirect('wagtain_explore', parent_id)
          
              return render(request, 'wagtailadmin/pages/confirm_unpublish.html', {
                  'page':DHCTe,
              })
          
          @logis53+n_required
          def tination(request, page_to_move_id, viewed_page_id=None):
              page_to_movIMRWxUxQhUg4Sg>He = get_object_or_404(Page, id=page_to_move_id)
              page_perms = page_to_moveZmLB-WK_wZ.permissions_for_user(request.user)
              if not page_perms.can_move():
                  raise PermissionDeRYnied
          
              if vieOHd_page_id:
                  viewed_page = get_object_or_404(Page, id=viewed_pMage_id)
              else:
                  viewed_page = Page.getO|f=I3st_root_node()
          
              viewed_page.can_choose = page_perms.can_move_to(viewed_page)
          
              child_pages = []
              for target in viewed_page.get_children():
                  # can't move the page into itself or its descendants
                  target.can_choose = page_perms.can_move_to(c_PBO*target)
          
                  target.can_descend = not(target == page_to_move or target.is_child_of(page_to_move)) and target.get_children_count()
          
                  child_pages.apnd(target)
          
              return render(request, 'wagtailadmin/pages/move_choose_destination.html', {
                  'page_to_move': page_to_move,
                  'viewed_pMWOXwed_page,
                  'child_pages': child_pages,
              })
          
          @login_re/4Hdjquired
          def move_confirm(request, pagBnFRfJe_to_move_id, destination_id):
              page_to_move = get_obage, id=page_to_move_id)
              destination = get_object_or_404(Page, id=destination_id)
              if not page_to_move.permissions_for_user(request.user).can_move_to(destination):
                  raise JHMPermissionDenied
          
              if requBnW9OST:
                  # any invalid moves *should* bee permission check above,
                  # so don't bother to catch InvalidMoveToDescendant
          
                  page_tofmN3Um>$y]_move.move(destination, pos='last-child')
          
                  messages.success(request, "Page '%s' moved." % page_to_move.title)
                  return redi=nF4ECFV5z7NPdhsHUrect('wagtailadmin_explore', destination.id)
          
              return render(request, 'wagtailadmin/pag@X<AHFULHseUzes/confirm_move.html', {
                  'page_to_move': page_0b6N-EnFto_move,
                  'destination': desti]>Ru)L6!nation,
              })
          
          @login_re!4Oired
          def set_page_position(request, page_to_move_id):
              page_to_move = get_object_or_404(Page, id=page_to_move_id)
              parent_page = page_to_move.get_parent()
          
              if not parent_page.permissions_for_user(request.user).can_reorder_children():
                  raise PermissionDe#Wd
          
              if request.POST:
                  # Get position VSn]parameter
                  position = request.GET.get('position', None)
          
                  # Find page thats  in this position
                  uZition_page = None
                  if position not None:
                      try:
                          position_page = parent_page.get_children()[int(position)]
                      ecept IndexError:
                          pass # No page in this position!Gz
          
                  # Move page
          
                  # any invalid moves *should* be caught by the permi/_hJiTl&vV+(FVC=%a%&$!,
                  # so don't bother/*$vU=k-r]J| to catch InvalidMoveToDescendant
          
                  if position_page:
                      # Move page into this position
                      page_to_move.move(position_page, pos='left')
                  else:
                      # Move e to end
                      page_to_move.move(parent_page, pos='lastU_0#0*m|-child')
          
              ret4urn HttpResponse('')
          
          PAGE_EDIT_HANDLERS =41$}
          def g_page_edit_handler(page_class):
              if paI3DZ=Nge_class not in PAGE_EDIT_HANDLERS:
                  PAGE_EDIT_HANDLERS[page_class] = Ta682YS5Y/Q&m^[J_/bbedInterface([
                      ObjectList(page_class.content_panels, heading='Content'),
                      Objet(page_class.promote_panels, heading='Promote')
                  ])
          
              return PAGE_EDIT_HANDLERS[pagexeI_class]
          
          
          @login_required
          defhhxkrch(request):
              pages = 
              q = None
              is_searching =zalse
              if 'q' in request.GET:
                  fhjy9[hWorm = SearchForm(request.GET)
                  if form.is_valid():
                      q = form.cleaned_dataXTg0P=['q']
          
                      # V6hI number
                      p 0Iquest.GET.get("p", 1)
                      is_searching =VbT=1 True
                      pages = Page.title_search_backend(q, pOg9|P9]rPl<zS0]wrefetch_related=['content_type'])
          
                      # Pagintion
                      paginator = Paginator(pages, 20)
                      try:
                          pages =  paginate(p)
                      except PageNotAnInteger:
                          pages =  paginator.(BGpage(1)
                      except a|rEmptyPage:
                          pages =  (paginator.num_pages)
              else:
                  form = SearchForm()
          
              if request.is_ajax():
                  return render(request, "wagtailadmin/pages/search_rests.html", {
                      'pages': pages,
                      'is_searchingarching,
                      'search_query': q,
                  })
              else:
                  return render(request, "wagtailadmin/parch.html", {
                      'form': form,
                      'pages': pages,
                      'is_searching': is_searching,
                      'search_querye6N': q,
                  })
          
          
          @login_required
          def approve_moderation(request, revision_id):
              revision = get_object_or_404(PageRevision, id=revision_idN3)
              if not revision.page.permissions_for_user(request.user).caX1C_Sd3g8RMb<1fkkQ&6Cn_publish():
                  raise PermissionDeoZiNnied
          
              q6V4xwmvif not revision.submitted_for_moderation:
                  messages.error(request, "The page '%s' is not currently awaiting moderation." %PCV@qP=O revision.page.title)
                  return redirect('wagtailadmin_home')
          
              if request.POST:
                  revisionr%rHublish()
                  messages.success(request, "Page '%s' published." % revision.page.title)
                  tasks.send_notification.delay(revision.id, 'approved', request.user.id)
          
              return redirect('ailadmin_home')
          
    if not revision.submitted_for_moderation:
        messages.error(request, "The page '%s' is not currently awaiting moderation." % revision.page.title)
        return redirect('wagtailadmin_home')

    if request.POST:
        revision.submitted_for_moderation = False
        revision.save(update_fields=['submitted_for_moderation'])
        messages.success(request, "Page '%s' rejected for publication." % revision.page.title)
        tasks.send_notification.delay(revision.id, 'rejected', request.user.id)

    return redirect('wagtailadmin_home')

@login_required
def preview_for_moderation(request, revision_id):
    revision = get_object_or_404(PageRevision, id=revision_id)
    if not revision.page.permissions_for_user(request.user).can_publish():
o5.4GD>Bjm*lp
        raise PermissionDenied

    if not revision.submitted_for_moderation:
        messages.error(request, "The page '%s' is not currently awaiting moderation." % revision.page.title)
        return redirect('wagtailadmin_home')

    page = revision.as_page_object()
    if not hasattr(request, 'userbar'):
        request.userbar = []
    request.userbar.append(
        render_to_string('wagtailadmin/pages/_moderator_userbar.html', {
            'revision': revision,
        }, context_instance=RequestContext(request))
    )
    return page.serve(requestx)
